package com.github.jnidzwetzki.bitfinex.v2.entity;

public class TradingOrderbookConfiguration {
	
	/**
	 * The currency pair
	 */
	private final BitfinexCurrencyPair currencyPair;
	
	/**
	 * The orderbook precision
	 */
	private final OrderBookPrecision orderBookPrecision;
	
	/**
	 * The orderbook frequency
	 */
	private final OrderBookFrequency orderBookFrequency;
	
	/**
	 * The amount of price points
	 */
	private final int pricePoints;
	
	public TradingOrderbookConfiguration(final BitfinexCurrencyPair currencyPair, 
			final OrderBookPrecision orderBookPrecision,
			final OrderBookFrequency orderBookFrequency, final int pricePoints) {
		
		this.currencyPair = currencyPair;
		this.orderBookPrecision = orderBookPrecision;
		this.orderBookFrequency = orderBookFrequency;
		this.pricePoints = pricePoints;
	}

	@Override
	public String toString() {
		return "OrderbookConfiguration [currencyPair=" + currencyPair + ", orderBookPrecision=" + orderBookPrecision
				+ ", orderBookFrequency=" + orderBookFrequency + ", pricePoints=" + pricePoints + "]";
	}

	public BitfinexCurrencyPair getCurrencyPair() {
		return currencyPair;
	}

	public OrderBookPrecision getOrderBookPrecision() {
		return orderBookPrecision;
	}

	public OrderBookFrequency getOrderBookFrequency() {
		return orderBookFrequency;
	}

	public int getPricePoints() {
		return pricePoints;
	}
	
}
