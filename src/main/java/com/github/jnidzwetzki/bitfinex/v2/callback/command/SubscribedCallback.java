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
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;

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

		if (channel.equals("ticker")) {
			final String symbol = jsonObject.getString("symbol");
			final BitfinexCurrencyPair currencyPair = BitfinexCurrencyPair.fromSymbolString(symbol);
			logger.info("Registering symbol {} on channel {}", currencyPair, channelId);
			bitfinexApiBroker.addToChannelSymbolMap(channelId, currencyPair);
		} else if (channel.equals("candles")) {
			final String key = jsonObject.getString("key");
			logger.info("Registering key {} on channel {}", key, channelId);
			final BitfinexCandlestickSymbol symbol = BitfinexCandlestickSymbol.fromBitfinexString(key);
			bitfinexApiBroker.addToChannelSymbolMap(channelId, symbol);
		} else if("book".equals(channel)) {
			final OrderbookConfiguration configuration 
				= OrderbookConfiguration.fromJSON(jsonObject);
			logger.info("Registering book {} on channel {}", jsonObject, channelId);
			bitfinexApiBroker.addToChannelSymbolMap(channelId, configuration);
		} else {
			logger.error("Unknown subscribed callback {}", jsonObject.toString());
		}
	
	}

}
