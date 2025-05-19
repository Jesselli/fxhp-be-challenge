package com.jesselli.fxhp_be_challenge.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jesselli.fxhp_be_challenge.model.MatchOrder;
import com.jesselli.fxhp_be_challenge.model.Order;
import com.jesselli.fxhp_be_challenge.model.Order.Direction;
import com.jesselli.fxhp_be_challenge.repository.OrderRepository;

/**
 * MatchController
 */
@RestController
@RequestMapping("/api")
public class MatchController {

	private final OrderRepository orderRepository;

	public MatchController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	/**
	 * Used to represent two matched orders -- typically one on the buy side
	 * and one on the sell side.
	 */
	public record OrderPair(Order order1, Order order2) {
	}

	/**
	 * If the one order is a buy and the other is a sell, this function updates
	 * the match values based on the current "amount" and "match" for each.
	 */
	public OrderPair updateOrderPair(Order order1, Order order2) {
		if (order1.direction.equals(order2.direction)) {
			return new OrderPair(order1, order2);
		}

		Order buyOrder = order1.direction.equals(Direction.BUY) ? order1 : order2;
		Order sellOrder = order1.direction.equals(Direction.SELL) ? order1 : order2;

		Double buyRemaining = buyOrder.amount - (buyOrder.match * buyOrder.amount);
		Double sellRemaining = sellOrder.amount - (sellOrder.match * sellOrder.amount);

		Double delta = Math.min(buyRemaining, sellRemaining);

		if (order1.userId.equals(order2.userId)) {
			// If it's the same user for both orders but they are opposite directions,
			// we don't want to count this as a match. We just want to update the overall
			// total for this currency pair.
			if ((order1.amount - delta) > 0) {
				order1.match = (order1.match * order1.amount) / (order1.amount - delta);
			}
			order1.amount -= delta;

			if ((order2.amount - delta) > 0) {
				order2.match = (order1.match * order2.amount) / (order2.amount - delta);
			}
			order2.amount -= delta;
		} else {
			order1.match = order1.match + (delta / order1.amount);
			order2.match = order2.match + (delta / order2.amount);
		}

		return new OrderPair(order1, order2);
	}

	@GetMapping("/match/{userId}")
	public ResponseEntity<List<MatchOrder>> getMatch(@PathVariable("userId") String userId) {
		HashMap<String, List<Order>> ordersWithMatchValues = calculateMatchesForAllOrders();
		Collection<Order> aggResults = aggregateMatchedOrdersForUser(userId, ordersWithMatchValues);

		List<MatchOrder> matchAggResults = new ArrayList<>();
		for (Order order : aggResults) {
			matchAggResults.add(MatchOrder.FromOrder(order));
		}
		return ResponseEntity.ok(matchAggResults);
	}

	/**
	 * Calculates the how much of each order has been matched on a "first come
	 * first matched" basis.
	 */
	private HashMap<String, List<Order>> calculateMatchesForAllOrders() {
		List<Order> allOrders = orderRepository.findAll();

		// As we process the orders and calculate their match values,
		// we add them to this map based on their currPair+dealtCurr+valueDate
		// identifier.
		HashMap<String, List<Order>> ordersWithMatchValues = new HashMap<>();
		for (Order order : allOrders) {
			String key = order.getKey();
			List<Order> existingOrders = ordersWithMatchValues.getOrDefault(key, new ArrayList<Order>());

			boolean orderCombined = false;

			// Iterate through the previously-processed orders to see if there
			// are any that are NOT fully matched and can be satisfied
			// by this order
			for (int i = 0; i < existingOrders.size(); i++) {
				Order existingOrder = existingOrders.get(i);
				if (existingOrder.match == 1.0) {
					continue;
				}

				if (order.match == 1.0) {
					break;
				}

				OrderPair updated = updateOrderPair(existingOrder, order);
				existingOrder = updated.order1;
				order = updated.order2;

				// If the users were the same and the amount remaining for the order
				// is zero after we update the orders, then we want to remove it from
				// the existing orders list. We have combined the orders.
				if (existingOrder.amount == 0) {
					existingOrders.remove(i);
				} else {
					existingOrders.set(i, existingOrder);
				}

				if (order.amount == 0) {
					orderCombined = true;
					break;
				}
			}

			if (!orderCombined) {
				existingOrders.add(order);
			}
			ordersWithMatchValues.put(key, existingOrders);
		}
		return ordersWithMatchValues;
	}

	/**
	 * Tally the match percentages for all orders in the map that correspond
	 * to the given userId. Returns a Collection of all the user's aggregated
	 * orders, each with match percentages.
	 *
	 * @param userId   The userId whose orders need to be aggregated
	 * @param orderMap A map of all orders with match values set individually
	 */
	private Collection<Order> aggregateMatchedOrdersForUser(String userId, HashMap<String, List<Order>> orderMap) {
		HashMap<String, Order> aggResults = new HashMap<>();
		for (List<Order> orders : orderMap.values()) {
			for (Order order : orders) {
				if (!order.userId.equals(userId)) {
					continue;
				}

				String key = order.getKey();
				Order aggOrder = aggResults.getOrDefault(key, order);
				if (aggOrder != order) {
					aggOrder = aggResults.get(key);
					aggOrder.addOrderToAggregate(order);
				}
				aggResults.put(key, aggOrder);
			}
		}
		return aggResults.values();
	}

}
