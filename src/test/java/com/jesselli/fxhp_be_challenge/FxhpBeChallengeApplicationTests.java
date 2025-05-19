package com.jesselli.fxhp_be_challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.jesselli.fxhp_be_challenge.repository.OrderRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jesselli.fxhp_be_challenge.model.Order;
import com.jesselli.fxhp_be_challenge.model.Order.Direction;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FxhpBeChallengeApplicationTests {
	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private OrderRepository orderRepository;

	@AfterEach
	void cleanUpDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void testScenarioA() {
		// "Scenario A" as defined in the Acceptance Criteria
		Order order = new Order("EURUSD", "USD", Direction.SELL, 10000.0, "20250130", "User A");
		ResponseEntity<Order> response = restTemplate.postForEntity("/api/order", order, Order.class);
		Order responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(10000.0, responseOrder.amount);
		assertEquals(Direction.SELL, responseOrder.direction);
		assertEquals("User A", responseOrder.userId);

		order = new Order("EURUSD", "USD", Direction.BUY, 5000.0, "20250130", "User A");
		response = restTemplate.postForEntity("/api/order", order, Order.class);
		responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(5000.0, responseOrder.amount);
		assertEquals(Direction.SELL, responseOrder.direction);
		assertEquals("User A", responseOrder.userId);

		order = new Order("EURUSD", "USD", Direction.BUY, 5000.0, "20250130", "User B");
		response = restTemplate.postForEntity("/api/order", order, Order.class);
		responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(5000.0, responseOrder.amount);
		assertEquals(Direction.BUY, responseOrder.direction);
		assertEquals("User B", responseOrder.userId);
	}

	@Test
	void testScenarioB() {
		// "Scenario B" as defined in the Acceptance Criteria
		Order order = new Order("EURUSD", "USD", Direction.SELL, 10000.0, "20250130", "User A");
		ResponseEntity<Order> response = restTemplate.postForEntity("/api/order", order, Order.class);
		Order responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(10000.0, responseOrder.amount);
		assertEquals(Direction.SELL, responseOrder.direction);

		order = new Order("EURUSD", "USD", Direction.BUY, 5000.0, "20250130", "User A");
		response = restTemplate.postForEntity("/api/order", order, Order.class);
		responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(5000.0, responseOrder.amount);
		assertEquals(Direction.SELL, responseOrder.direction);

		order = new Order("EURUSD", "USD", Direction.BUY, 20000.0, "20250130", "User C");
		response = restTemplate.postForEntity("/api/order", order, Order.class);
		responseOrder = response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(20000.0, responseOrder.amount);
		assertEquals(Direction.BUY, responseOrder.direction);

		ResponseEntity<Collection<Order>> matchResponseA = restTemplate.exchange(
				"/api/match/User A",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Collection<Order>>() {
				});
		assertEquals(HttpStatus.OK, matchResponseA.getStatusCode());
		List<Order> matchOrdersA = new ArrayList<>(matchResponseA.getBody());
		Order firstMatchOrderA = matchOrdersA.get(0);
		assertEquals(1, firstMatchOrderA.match);

		ResponseEntity<Collection<Order>> matchResponseC = restTemplate.exchange(
				"/api/match/User C",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Collection<Order>>() {
				});
		assertEquals(HttpStatus.OK, matchResponseC.getStatusCode());
		List<Order> matchOrdersC = new ArrayList<>(matchResponseC.getBody());
		Order firstMatchOrderC = matchOrdersC.get(0);
		assertEquals(0.25, firstMatchOrderC.match);
	}
}
