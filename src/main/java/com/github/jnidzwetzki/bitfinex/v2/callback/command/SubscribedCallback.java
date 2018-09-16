/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *  
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *    
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.callback.command;

import java.util.function.BiConsumer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class SubscribedCallback implements CommandCallbackHandler {

	private final static Logger logger = LoggerFactory.getLogger(SubscribedCallback.class);

	private BiConsumer<Integer, BitfinexStreamSymbol> subscribeResultConsumer = (i, s) -> {};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final JSONObject jsonObject) throws BitfinexClientException {
		final String channelType = jsonObject.getString("channel");
		final int channelId = jsonObject.getInt("chanId");

		BitfinexStreamSymbol symbol = null;
		switch (channelType) {
			case "ticker":
				symbol = handleTickerCallback(jsonObject);
				break;
			case "trades":
				symbol = handleTradesCallback(jsonObject);
				break;
			case "candles":
				symbol = handleCandlesCallback(jsonObject);
				break;
			case "book":
				symbol = handleBookCallback(jsonObject);
				break;
			default:
				logger.error("Unknown subscribed callback {}", jsonObject.toString());
		}
		if (symbol != null) {
			subscribeResultConsumer.accept(channelId, symbol);
		}
	}

	/**
	 * subscribed event consumer
	 * @param consumer of event
	 */
	public void onSubscribedEvent(BiConsumer<Integer, BitfinexStreamSymbol> consumer) {
		this.subscribeResultConsumer = consumer;
	}

	private BitfinexStreamSymbol handleBookCallback(final JSONObject jsonObject) {
		BitfinexStreamSymbol symbol;
		if("R0".equals(jsonObject.getString("prec"))) {
			symbol = BitfinexOrderBookSymbol.fromJSON(jsonObject);
		} else {
			symbol = BitfinexOrderBookSymbol.fromJSON(jsonObject);
		}
		return symbol;
	}

	private BitfinexCandlestickSymbol handleCandlesCallback(final JSONObject jsonObject) {
		final String key = jsonObject.getString("key");
		return BitfinexCandlestickSymbol.fromBitfinexString(key);
	}

	private BitfinexExecutedTradeSymbol handleTradesCallback(final JSONObject jsonObject) {
		final String key = jsonObject.getString("symbol");
		return BitfinexExecutedTradeSymbol.fromBitfinexString(key);
	}

	private BitfinexTickerSymbol handleTickerCallback(final JSONObject jsonObject) {
		final String key = jsonObject.getString("symbol");
		return BitfinexTickerSymbol.fromBitfinexString(key);
	}
}
