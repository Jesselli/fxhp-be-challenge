package com.jesselli.fxhp_be_challenge.model;

import java.time.Instant;

import com.jesselli.fxhp_be_challenge.model.Order.Direction;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "orders")
public class Order {
	public enum Direction {
		BUY, SELL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	@NotNull
	@jakarta.validation.constraints.Pattern(regexp = "^[A-Z]{6}$", message = "currencyPair must be exactly six capital letters")
	public String currencyPair;

	@NotNull
	@jakarta.validation.constraints.Pattern(regexp = "^[A-Z]{3}$", message = "dealtCurrency must be 3 capital lettes (ISO 4217) ")
	public String dealtCurrency;

	@NotNull
	@Enumerated(EnumType.STRING)
	public Direction direction;

	@NotNull
	@Positive
	public Double amount;

	@NotNull
	@jakarta.validation.constraints.Pattern(regexp = "^\\d{8}$", message = "valueDate must be in format YYYYMMDD")
	public String valueDate;

	@NotNull
	public String userId;

	public Long createdAt;

	public Double match = 0.0;

	public Order() {
	}

	public Order(String currencyPair,
			String dealtCurrency,
			Direction direction,
			Double amount,
			String valueDate,
			String userId) {
		this.currencyPair = currencyPair;
		this.dealtCurrency = dealtCurrency;
		this.direction = direction;
		this.amount = amount;
		this.valueDate = valueDate;
		this.userId = userId;
	}

	@PrePersist
	public void onCreate() {
		this.createdAt = Instant.now().toEpochMilli();
	}

	public String getKey() {
		return this.currencyPair + "-" + this.dealtCurrency + "-" + this.valueDate;
	}

	/**
	 * Tallies the overall amount and match percentage after adding
	 * another order's match and amount.
	 */
	public void addOrderToAggregate(Order order) {
		double totalAmount = this.amount + order.amount;
		this.match = ((this.match * this.amount)
				+ (order.match * order.amount))
				/ totalAmount;
		this.amount = totalAmount;
	}
}
