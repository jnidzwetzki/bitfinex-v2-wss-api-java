package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class SubscribeTickerCommand extends AbstractAPICommand {

	private String currencyPair;

	public SubscribeTickerCommand(final BitfinexCurrencyPair currencyPair) {
		this.currencyPair = currencyPair.toBitfinexString();
	}
	
	public SubscribeTickerCommand(final String currencyPair) {
		this.currencyPair = currencyPair;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "ticker");
		subscribeJson.put("symbol", currencyPair);
		
		return subscribeJson.toString();
	}
}
