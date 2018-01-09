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
package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;

public class SubscribeTradingOrderbookCommand extends AbstractAPICommand {

	/**
	 * The orderbook configuration
	 */
	private OrderbookConfiguration orderbookConfiguration;
	
	public SubscribeTradingOrderbookCommand(final OrderbookConfiguration orderbookConfiguration) {
		this.orderbookConfiguration = orderbookConfiguration;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "book");
		subscribeJson.put("symbol", orderbookConfiguration.getCurrencyPair().toBitfinexString());
		subscribeJson.put("prec", orderbookConfiguration.getOrderBookPrecision().toString());
		subscribeJson.put("freq", orderbookConfiguration.getOrderBookFrequency().toString());
		subscribeJson.put("len", Integer.toString(orderbookConfiguration.getPricePoints()));

		return subscribeJson.toString();
	}

}
