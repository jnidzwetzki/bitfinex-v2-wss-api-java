package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;

public class SubscribeOrderbookCommand extends AbstractAPICommand {

	/**
	 * The currency pair
	 */
	private BitfinexCurrencyPair currencyPair;
	
	/**
	 * The order book precision
	 */
	private OrderBookPrecision orderBookPrecision;
	
	/**
	 * The order book frequeny
	 */
	private OrderBookFrequency orderBookFrequency;
	
	/**
	 * The amount of price points
	 */
	private int pricePoints;

	public SubscribeOrderbookCommand(final BitfinexCurrencyPair currencyPair, 
			final OrderBookPrecision orderBookPrecision, final OrderBookFrequency orderBookFrequency, 
			final int pricePoints) {
				this.currencyPair = currencyPair;
				this.orderBookPrecision = orderBookPrecision;
				this.orderBookFrequency = orderBookFrequency;
				this.pricePoints = pricePoints;
	}
	
	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "book");
		subscribeJson.put("symbol", currencyPair);
		subscribeJson.put("prec", orderBookPrecision.toString());
		subscribeJson.put("freq", orderBookFrequency.toString());
		subscribeJson.put("len", Integer.toString(pricePoints));

		return subscribeJson.toString();
	}

}
