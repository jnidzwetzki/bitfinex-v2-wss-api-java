package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.Timeframe;

public class SubscribeCandlesCommand extends AbstractAPICommand {

	private BitfinexCurrencyPair currencyPair;

	private Timeframe timeframe;

	public SubscribeCandlesCommand(final BitfinexCurrencyPair currencyPair, final Timeframe timeframe) {
		this.currencyPair = currencyPair;
		this.timeframe = timeframe;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "candles");
		subscribeJson.put("key", "trade:" + timeframe.getBitfinexString() + ":" + currencyPair.toBitfinexString());
		
		System.out.println(subscribeJson.toString());
		
		return subscribeJson.toString();
	}

}
