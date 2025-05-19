package com.jesselli.fxhp_be_challenge.model;

import com.jesselli.fxhp_be_challenge.model.Order.Direction;

// Currency Pair (EURUSD)
// Dealt Currency (USD)
// Direction (SELL)
// Value Date (20250130)
// User ID (User A)
// Amount 
// Match
public class MatchOrder {
	public String currencyPair;
	public String dealtCurrency;
	public Direction direction;
	public String valueDate;
	public String userId;
	public Double amount;
	public Double match;

	public static MatchOrder FromOrder(Order order) {
		MatchOrder matchOrder = new MatchOrder();
		matchOrder.currencyPair = order.currencyPair;
		matchOrder.dealtCurrency = order.dealtCurrency;
		matchOrder.direction = order.direction;
		matchOrder.valueDate = order.valueDate;
		matchOrder.userId = order.userId;
		matchOrder.amount = order.amount;
		matchOrder.match = order.match;
		return matchOrder;
	}
}
