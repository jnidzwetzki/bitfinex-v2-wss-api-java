package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradingOrderbookConfiguration;

public class SubscribeTradingOrderbookCommand extends AbstractAPICommand {

	/**
	 * The orderbook configuration
	 */
	private TradingOrderbookConfiguration orderbookConfiguration;
	
	public SubscribeTradingOrderbookCommand(final TradingOrderbookConfiguration orderbookConfiguration) {
		this.orderbookConfiguration = orderbookConfiguration;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "book");
		subscribeJson.put("symbol", orderbookConfiguration.getCurrencyPair());
		subscribeJson.put("prec", orderbookConfiguration.getOrderBookPrecision().toString());
		subscribeJson.put("freq", orderbookConfiguration.getOrderBookFrequency().toString());
		subscribeJson.put("len", Integer.toString(orderbookConfiguration.getPricePoints()));

		return subscribeJson.toString();
	}

}
