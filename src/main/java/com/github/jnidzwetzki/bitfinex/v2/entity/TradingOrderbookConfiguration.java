package com.github.jnidzwetzki.bitfinex.v2.entity;

import org.json.JSONObject;

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
		
		if(pricePoints < 25 || pricePoints > 100) {
			throw new IllegalArgumentException("Price points must be between 25 and 100");
		}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyPair == null) ? 0 : currencyPair.hashCode());
		result = prime * result + ((orderBookFrequency == null) ? 0 : orderBookFrequency.hashCode());
		result = prime * result + ((orderBookPrecision == null) ? 0 : orderBookPrecision.hashCode());
		result = prime * result + pricePoints;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradingOrderbookConfiguration other = (TradingOrderbookConfiguration) obj;
		if (currencyPair != other.currencyPair)
			return false;
		if (orderBookFrequency != other.orderBookFrequency)
			return false;
		if (orderBookPrecision != other.orderBookPrecision)
			return false;
		if (pricePoints != other.pricePoints)
			return false;
		return true;
	}

	/**
	 * Build from JSON Array
	 * @param jsonObject
	 * @return
	 */
	public static TradingOrderbookConfiguration fromJSON(final JSONObject jsonObject) {
		return new TradingOrderbookConfiguration(
				BitfinexCurrencyPair.fromSymbolString(jsonObject.getString("symbol")), 
				OrderBookPrecision.valueOf(jsonObject.getString("prec")), 
				OrderBookFrequency.valueOf(jsonObject.getString("freq")), 
				jsonObject.getInt("len"));
	}
	
}
