package com.jesselli.fxhp_be_challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jesselli.fxhp_be_challenge.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	public static String AGG_QUERY_FOR_USER = """
			SELECT new com.jesselli.fxhp_be_challenge.model.Order(
				o.currencyPair,
				o.dealtCurrency,
				CASE WHEN SUM(CASE WHEN o.direction = 'BUY' THEN o.amount ELSE -o.amount END) >= 0 THEN com.jesselli.fxhp_be_challenge.model.Order.Direction.BUY ELSE com.jesselli.fxhp_be_challenge.model.Order.Direction.SELL END,
				ABS(SUM(CASE WHEN o.direction = 'BUY' THEN o.amount ELSE -o.amount END)),
				o.valueDate,
				o.userId
			)
			FROM Order o
			WHERE o.userId = :userId
			AND o.currencyPair = :currencyPair
			AND o.dealtCurrency = :dealtCurrency
			AND o.valueDate = :valueDate
			GROUP BY
				o.currencyPair,
				o.dealtCurrency,
				o.valueDate,
				o.userId
			""";

	@Query(AGG_QUERY_FOR_USER)
	List<Order> aggregateOrdersForUserId(@Param("userId") String userId,
			@Param("currencyPair") String currencyPair,
			@Param("dealtCurrency") String dealtCurrency,
			@Param("valueDate") String valueDate);

	List<Order> findByUserId(String userId);

	void deleteAll();
}
