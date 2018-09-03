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

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;

public class SubscribedCallback implements CommandCallbackHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(SubscribedCallback.class);

	private BiConsumer<Integer, BitfinexStreamSymbol> subscribeResultConsumer = (i, s) -> {};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final JSONObject jsonObject) throws APIException {
		final String channel = jsonObject.getString("channel");
		final int channelId = jsonObject.getInt("chanId");

		switch (channel) {
			case "ticker":
				handleTickerCallback(jsonObject, channelId);
				break;
			case "trades":
				handleTradesCallback(jsonObject, channelId);
				break;
			case "candles":
				handleCandlesCallback(jsonObject, channelId);
				break;
			case "book":
				handleBookCallback(jsonObject, channelId);
				break;
			default:
				LOGGER.error("Unknown subscribed callback {}", jsonObject.toString());
		}
	}

	/**
	 * subscribed event consumer
	 * @param consumer of event
	 */
	public void onSubscribedEvent(BiConsumer<Integer, BitfinexStreamSymbol> consumer) {
		this.subscribeResultConsumer = consumer;
	}

	private void handleBookCallback(final JSONObject jsonObject, final int channelId) {
		BitfinexStreamSymbol symbol;
		if("R0".equals(jsonObject.getString("prec"))) {
			symbol = RawOrderbookConfiguration.fromJSON(jsonObject);
			LOGGER.info("Registering raw book {} on channel {}", jsonObject, channelId);
		} else {
			symbol = OrderbookConfiguration.fromJSON(jsonObject);
			LOGGER.info("Registering book {} on channel {}", jsonObject, channelId);
		}
		subscribeResultConsumer.accept(channelId, symbol);
	}

	private void handleCandlesCallback(final JSONObject jsonObject, final int channelId) {
		final String key = jsonObject.getString("key");
		final BitfinexCandlestickSymbol symbol = BitfinexCandlestickSymbol.fromBitfinexString(key);
		LOGGER.info("Registering key {} on channel {}", key, channelId);
		subscribeResultConsumer.accept(channelId, symbol);
	}

	private void handleTradesCallback(final JSONObject jsonObject, final int channelId) {
		final String key = jsonObject.getString("symbol");
		final BitfinexExecutedTradeSymbol symbol = BitfinexExecutedTradeSymbol.fromBitfinexString(key);
		LOGGER.info("Registering symbol {} on channel {}", symbol, channelId);
		subscribeResultConsumer.accept(channelId, symbol);
	}

	private void handleTickerCallback(final JSONObject jsonObject, final int channelId) {
		final String key = jsonObject.getString("symbol");
		final BitfinexTickerSymbol symbol = BitfinexTickerSymbol.fromBitfinexString(key);
		LOGGER.info("Registering symbol {} on channel {}", symbol, channelId);
		subscribeResultConsumer.accept(channelId, symbol);
	}
}
