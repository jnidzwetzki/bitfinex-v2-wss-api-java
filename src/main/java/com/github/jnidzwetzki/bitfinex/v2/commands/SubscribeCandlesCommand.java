package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;

public class SubscribeCandlesCommand extends AbstractAPICommand {

	private final BitfinexCandlestickSymbol symbol;

	public SubscribeCandlesCommand(final BitfinexCandlestickSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "candles");
		subscribeJson.put("key", symbol.toBifinexCandlestickString());
				
		return subscribeJson.toString();
	}

}
