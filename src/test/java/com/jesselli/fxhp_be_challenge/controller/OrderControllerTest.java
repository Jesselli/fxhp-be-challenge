package com.jesselli.fxhp_be_challenge.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesselli.fxhp_be_challenge.model.Order;
import com.jesselli.fxhp_be_challenge.model.Order.Direction;
import com.jesselli.fxhp_be_challenge.repository.OrderRepository;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderRepository orderRepository;

	@Test
	void testAddOrder() throws Exception {
		Order testOrder = new Order("EURUSD", "EUR", Direction.BUY, 100.00, "20250519", "userA");
		Order aggOrder = new Order("EURUSD", "EUR", Direction.BUY, 100.00, "20250519", "userA");
		List<Order> aggOrderList = new ArrayList<>();
		aggOrderList.add(aggOrder);

		when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
		when(orderRepository.aggregateOrdersForUserId(
				eq(testOrder.userId),
				eq(testOrder.currencyPair),
				eq(testOrder.dealtCurrency),
				eq(testOrder.valueDate))).thenReturn(aggOrderList);

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testOrder)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.currencyPair").value(aggOrder.currencyPair))
				.andExpect(jsonPath("$.dealtCurrency").value(aggOrder.dealtCurrency))
				.andExpect(jsonPath("$.direction").value(aggOrder.direction.toString()))
				.andExpect(jsonPath("$.amount").value(aggOrder.amount))
				.andExpect(jsonPath("$.valueDate").value(aggOrder.valueDate))
				.andExpect(jsonPath("$.userId").value(aggOrder.userId));

		verify(orderRepository, times(1)).save(any(Order.class));
		verify(orderRepository, times(1)).flush();
		verify(orderRepository, times(1)).aggregateOrdersForUserId(
				eq(testOrder.userId),
				eq(testOrder.currencyPair),
				eq(testOrder.dealtCurrency),
				eq(testOrder.valueDate));
	}

	@Test
	void testOrderWithZeroAmount() throws Exception {
		Order invalidOrder = new Order("USDJPY", "JPY", Direction.BUY, 0.0, "20250519", "userA");

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidOrder)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void testOrderWithInvalidPair() throws Exception {
		Order invalidOrder = new Order("!@#USD", "JPY", Direction.BUY, 1.0, "20250519", "userA");

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidOrder)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void testOrderWithInvalidDealtCurr() throws Exception {
		Order invalidOrder = new Order("USDCAD", "1@A", Direction.BUY, 1.0, "20250519", "userA");

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidOrder)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void testAddInvalidOrder() throws Exception {
		Order invalidOrder = new Order();

		mockMvc.perform(post("/api/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidOrder)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verify(orderRepository, never()).save(any(Order.class));
	}
}
