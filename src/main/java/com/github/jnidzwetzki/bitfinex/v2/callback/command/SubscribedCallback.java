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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;

public class SubscribedCallback implements CommandCallbackHandler {

	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(SubscribedCallback.class);
	
	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final JSONObject jsonObject) throws APIException {
		
		final String channel = jsonObject.getString("channel");
		final int channelId = jsonObject.getInt("chanId");

		switch(channel) {
		case "ticker":
			handleTickerCallback(bitfinexApiBroker, jsonObject, channelId);
			break;
		case "trades":
			handleTradesCallback(bitfinexApiBroker, jsonObject, channelId);
			break;
		case "candles":
			handleCandlesCallback(bitfinexApiBroker, jsonObject, channelId);
			break;
		case "book":
			handleBookCallback(bitfinexApiBroker, jsonObject, channelId);
			break;
		default:
			logger.error("Unknown subscribed callback {}", jsonObject.toString());
		}
	}

	/**
	 * Handle the book callback
	 * 
	 * @param bitfinexApiBroker
	 * @param jsonObject
	 * @param channelId
	 */
	private void handleBookCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONObject jsonObject,
			final int channelId) {
		
		if("R0".equals(jsonObject.getString("prec"))) {
			final RawOrderbookConfiguration configuration 
				= RawOrderbookConfiguration.fromJSON(jsonObject);
			logger.info("Registering raw book {} on channel {}", jsonObject, channelId);
			bitfinexApiBroker.addToChannelSymbolMap(channelId, configuration);
		} else {
			final OrderbookConfiguration configuration 
				= OrderbookConfiguration.fromJSON(jsonObject);
			logger.info("Registering book {} on channel {}", jsonObject, channelId);
			bitfinexApiBroker.addToChannelSymbolMap(channelId, configuration);
		}
	}

	/**
	 * Handle the candles callback
	 * 
	 * @param bitfinexApiBroker
	 * @param jsonObject
	 * @param channelId
	 */
	private void handleCandlesCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONObject jsonObject,
			final int channelId) {
		
		final String key = jsonObject.getString("key");
		logger.info("Registering key {} on channel {}", key, channelId);
		final BitfinexCandlestickSymbol candleStickSymbol = BitfinexCandlestickSymbol.fromBitfinexString(key);
		bitfinexApiBroker.addToChannelSymbolMap(channelId, candleStickSymbol);
	}

	/**
	 * Handle the trades callback
	 * 
	 * @param bitfinexApiBroker
	 * @param jsonObject
	 * @param channelId
	 */
	private void handleTradesCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONObject jsonObject,
			final int channelId) {
		
		final String symbol2 = jsonObject.getString("symbol");
		final BitfinexExecutedTradeSymbol currencyPair = BitfinexExecutedTradeSymbol.fromBitfinexString(symbol2);
		logger.info("Registering symbol {} on channel {}", currencyPair, channelId);
		bitfinexApiBroker.addToChannelSymbolMap(channelId, currencyPair);
	}

	/**
	 * Handle the ticker callback 
	 * 
	 * @param bitfinexApiBroker
	 * @param jsonObject
	 * @param channelId
	 */
	private void handleTickerCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONObject jsonObject,
			final int channelId) {
		
		final String symbol = jsonObject.getString("symbol");
		final BitfinexTickerSymbol tickerSymbol = BitfinexTickerSymbol.fromBitfinexString(symbol);
		logger.info("Registering symbol {} on channel {}", tickerSymbol, channelId);
		bitfinexApiBroker.addToChannelSymbolMap(channelId, tickerSymbol);
	}
}
