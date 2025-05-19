package com.jesselli.fxhp_be_challenge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jesselli.fxhp_be_challenge.model.AggOrder;
import com.jesselli.fxhp_be_challenge.model.Order;
import com.jesselli.fxhp_be_challenge.repository.OrderRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class OrderController {
	private final OrderRepository orderRepository;

	public OrderController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@GetMapping("/order")
	public ResponseEntity<List<Order>> getOrders() {
		return ResponseEntity.ok(orderRepository.findAll());
	}

	@GetMapping("/order/{userId}")
	public ResponseEntity<List<Order>> getOrdersForUser(@PathVariable("userId") String userId) {
		List<Order> orders = orderRepository.findByUserId(userId);
		return ResponseEntity.ok(orders);
	}

	@PostMapping("/order")
	public ResponseEntity<?> addOrder(@Valid @RequestBody Order order) {
		try {
			orderRepository.save(order);
			orderRepository.flush();

			List<Order> orders = orderRepository.aggregateOrdersForUserId(order.userId,
					order.currencyPair,
					order.dealtCurrency, order.valueDate);

			Order result = orders.getFirst();
			AggOrder aggResult = AggOrder.FromOrder(result);

			return ResponseEntity.ok(aggResult);
		} catch (jakarta.validation.ConstraintViolationException e) {
			// Acceptance criteria requires returning null in the case of an error
			return ResponseEntity.badRequest().body(null);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(null);
		}
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest().body(null);
	}
}
