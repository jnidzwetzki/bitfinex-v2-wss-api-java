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
package com.github.jnidzwetzki.bitfinex.v2.command;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class SubscribeTickerCommand implements SubscribeCommand {

	private final BitfinexTickerSymbol symbol;
	private final String currencyPair;

	public SubscribeTickerCommand(final BitfinexTickerSymbol currencyPair) {
		this.symbol = currencyPair;
		this.currencyPair = currencyPair.getCurrencyPair().toBitfinexString();
	}

	@Override
	public String getCommand(final BitfinexWebsocketClient client) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "ticker");
		subscribeJson.put("symbol", currencyPair);
		
		return subscribeJson.toString();
	}

	@Override
	public BitfinexStreamSymbol getSymbol() {
		return symbol;
	}
}
