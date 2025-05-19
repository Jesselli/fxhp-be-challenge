package com.jesselli.fxhp_be_challenge.model;

import com.jesselli.fxhp_be_challenge.model.Order.Direction;

// Currency Pair (EURUSD)
// Dealt Currency (USD)
// Direction (SELL)
// Amount (10000)
// Value Date (20250130)
// User ID (User A)
public class AggOrder {
	public String currencyPair;
	public String dealtCurrency;
	public Direction direction;
	public Double amount;
	public String valueDate;
	public String userId;

	public static AggOrder FromOrder(Order order) {
		AggOrder aggOrder = new AggOrder();
		aggOrder.currencyPair = order.currencyPair;
		aggOrder.dealtCurrency = order.dealtCurrency;
		aggOrder.direction = order.direction;
		aggOrder.amount = order.amount;
		aggOrder.valueDate = order.valueDate;
		aggOrder.userId = order.userId;
		return aggOrder;
	}
}
