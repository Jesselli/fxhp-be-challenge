package com.jesselli.fxhp_be_challenge.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesselli.fxhp_be_challenge.model.Order;
import com.jesselli.fxhp_be_challenge.model.MatchOrder;
import com.jesselli.fxhp_be_challenge.model.Order.Direction;
import com.jesselli.fxhp_be_challenge.repository.OrderRepository;
import com.jesselli.fxhp_be_challenge.controller.MatchController.OrderPair;

@WebMvcTest(MatchController.class)
public class MatchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderRepository orderRepository;

	@Test
	void testUpdateOrderPairDifferentDirections() {
		Order order1 = new Order("EURUSD", "EUR", Direction.BUY, 100.0, "20250519", "userA");
		Order order2 = new Order("EURUSD", "EUR", Direction.SELL, 50.0, "20250519", "userB");

		MatchController controller = new MatchController(orderRepository);
		OrderPair result = controller.updateOrderPair(order1, order2);

		// BUY order is partially matched (50/100), SELL order is fully matched (50/50)
		assertEquals(0.5, result.order1().match);
		assertEquals(1.0, result.order2().match);
	}

	@Test
	void testUpdateOrderPairSameDirection() {
		Order order1 = new Order("EURUSD", "EUR", Direction.BUY, 100.0, "20250519", "userA");
		Order order2 = new Order("EURUSD", "EUR", Direction.BUY, 50.0, "20250519", "userB");
		order1.match = 0.2; // Simulate pre-existing match values
		order2.match = 0.1;

		MatchController controller = new MatchController(orderRepository);
		OrderPair result = controller.updateOrderPair(order1, order2);

		// Values should be unchanged because the two orders are both BUY
		assertEquals(0.2, result.order1().match);
		assertEquals(0.1, result.order2().match);
	}

	@Test
	void testGetMatch() throws Exception {
		Order buyOrder = new Order("EURUSD", "EUR", Direction.BUY, 100.0, "20250519", "userA");
		Order sellOrder = new Order("EURUSD", "EUR", Direction.SELL, 50.0, "20250519", "userB");
		List<Order> orderList = new ArrayList<>();
		orderList.add(buyOrder);
		orderList.add(sellOrder);

		when(orderRepository.findAll()).thenReturn(orderList);

		// Check match for userA
		MvcResult resultA = mockMvc.perform(get("/api/match/userA"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String contentA = resultA.getResponse().getContentAsString();
		List<MatchOrder> responseOrdersA = objectMapper.readValue(contentA,
				new TypeReference<List<MatchOrder>>() {
				});

		assertFalse(responseOrdersA.isEmpty());
		assertEquals(1, responseOrdersA.size());
		MatchOrder firstOrderA = responseOrdersA.getFirst();
		assertEquals(100.0, firstOrderA.amount);
		assertEquals(0.5, firstOrderA.match);

		// Check match for userB
		MvcResult resultB = mockMvc.perform(get("/api/match/userB"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String contentB = resultB.getResponse().getContentAsString();
		List<MatchOrder> responseOrdersB = objectMapper.readValue(contentB,
				new TypeReference<List<MatchOrder>>() {
				});

		assertFalse(responseOrdersB.isEmpty());
		assertEquals(1, responseOrdersB.size());
		MatchOrder firstOrderB = responseOrdersB.getFirst();
		assertEquals(50.0, firstOrderB.amount);
		assertEquals(1.0, firstOrderB.match);

		verify(orderRepository, times(2)).findAll();
	}

	@Test
	void testDifferentValueDates() throws Exception {
		Order buyOrder = new Order("EURUSD", "EUR", Direction.BUY, 100.0, "20250519", "userA");
		Order sellOrder = new Order("EURUSD", "EUR", Direction.SELL, 50.0, "20250518", "userA");
		List<Order> orderList = new ArrayList<>();
		orderList.add(buyOrder);
		orderList.add(sellOrder);

		when(orderRepository.findAll()).thenReturn(orderList);

		MvcResult result = mockMvc.perform(get("/api/match/userA"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String content = result.getResponse().getContentAsString();
		Collection<Order> responseOrders = objectMapper.readValue(content,
				new TypeReference<Collection<Order>>() {
				});

		assertFalse(responseOrders.isEmpty());
		assertEquals(2, responseOrders.size());

		verify(orderRepository, times(1)).findAll();
	}

	@Test
	void testGetMatchNoMatches() throws Exception {
		when(orderRepository.findAll()).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/api/match/userA"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("[]"));

		verify(orderRepository, times(1)).findAll();
	}

	@Test
	void testAggregateMatchedOrdersForUser() throws Exception {
		// For EURUSD userA has an aggregated total buy of 250 and userB has a total
		// sell of 150
		Order buyOrder1 = new Order("EURUSD", "EUR", Direction.BUY, 100.0, "20250519", "userA");
		Order buyOrder2 = new Order("USDJPY", "EUR", Direction.SELL, 50.0, "20250519", "userB");
		Order buyOrder3 = new Order("EURUSD", "EUR", Direction.SELL, 25.0, "20250519", "userB");
		Order buyOrder4 = new Order("EURUSD", "EUR", Direction.BUY, 150.0, "20250519", "userA");
		Order buyOrder5 = new Order("EURUSD", "EUR", Direction.SELL, 125.0, "20250519", "userB");

		List<Order> userAOrderList = new ArrayList<>();
		userAOrderList.add(buyOrder1);
		userAOrderList.add(buyOrder2);
		userAOrderList.add(buyOrder3);
		userAOrderList.add(buyOrder4);
		userAOrderList.add(buyOrder5);

		when(orderRepository.findAll()).thenReturn(userAOrderList);

		MvcResult result = mockMvc.perform(get("/api/match/userA"))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();
		List<MatchOrder> responseOrders = objectMapper.readValue(content,
				new TypeReference<List<MatchOrder>>() {
				});

		// 150/(100+150) = 0.6
		assertEquals(1, responseOrders.size());
		MatchOrder responseOrder = responseOrders.getFirst();
		assertEquals("userA", responseOrder.userId);
		assertEquals(250.0, responseOrder.amount);
		assertEquals(0.6, responseOrder.match);
	}

	@Test
	void testBuySellSameUserAndCurrencyPair() throws Exception {
		// The first 3 orders should get combined/aggregated before we match with the
		// 4th order placed by another user
		Order buyOrder1 = new Order("EURUSD", "EUR", Direction.BUY, 10.0, "20250519", "userA");
		Order buyOrder2 = new Order("EURUSD", "EUR", Direction.SELL, 5.0, "20250519", "userA");
		Order buyOrder3 = new Order("EURUSD", "EUR", Direction.BUY, 1.0, "20250519", "userA");
		Order buyOrder4 = new Order("EURUSD", "EUR", Direction.SELL, 2.0, "20250519", "userB");

		List<Order> userAOrderList = new ArrayList<>();
		userAOrderList.add(buyOrder1);
		userAOrderList.add(buyOrder2);
		userAOrderList.add(buyOrder3);
		userAOrderList.add(buyOrder4);

		when(orderRepository.findAll()).thenReturn(userAOrderList);

		// Verfiy the match data for userA
		MvcResult resultA = mockMvc.perform(get("/api/match/userA"))
				.andExpect(status().isOk())
				.andReturn();

		String contentA = resultA.getResponse().getContentAsString();
		List<MatchOrder> responseOrdersA = objectMapper.readValue(contentA,
				new TypeReference<List<MatchOrder>>() {
				});

		// 2/(10-5+1) = 0.333
		assertEquals(1, responseOrdersA.size());
		MatchOrder responseOrderA = responseOrdersA.getFirst();
		assertEquals("userA", responseOrderA.userId);
		assertEquals(6.0, responseOrderA.amount);
		assertEquals(0.333, responseOrderA.match, 0.001);

		// Verfiy the match data for userB
		MvcResult resultB = mockMvc.perform(get("/api/match/userB"))
				.andExpect(status().isOk())
				.andReturn();

		String contentB = resultB.getResponse().getContentAsString();
		List<MatchOrder> responseOrdersB = objectMapper.readValue(contentB,
				new TypeReference<List<MatchOrder>>() {
				});

		// User B's order should be 100% matched
		assertEquals(1, responseOrdersB.size());
		MatchOrder responseOrderB = responseOrdersB.getFirst();
		assertEquals("userB", responseOrderB.userId);
		assertEquals(2.0, responseOrderB.amount);
		assertEquals(1.0, responseOrderB.match);
	}
}
