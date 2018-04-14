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
package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.Trade;

public class TradeHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(TradeHandler.class);

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {

		logger.info("Got trade callback {}", jsonArray.toString());
		
		final JSONArray trade = jsonArray.getJSONArray(2);
		final String type = jsonArray.getString(1);
		
		// Executed or update
		boolean executed = true;
		if("tu".equals(type)) {
			executed = false;
		}

		handleTradeCallback(bitfinexApiBroker, trade, executed);
	}


	/**
	 * Handle a single trade callback
	 * @param bitfinexApiBroker 
	 * @param orderArray
	 * @throws APIException 
	 */
	private void handleTradeCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonTrade,
			final boolean executed) throws APIException {		
		
		final Trade trade = new Trade();
		trade.setExecuted(executed);
		trade.setApikey(bitfinexApiBroker.getApiKey());
		trade.setId(jsonTrade.getLong(0));
		trade.setCurrency(BitfinexCurrencyPair.fromSymbolString(jsonTrade.getString(1)));
		trade.setMtsCreate(jsonTrade.getLong(2));
		trade.setOrderId(jsonTrade.getLong(3));
		trade.setExecAmount(jsonTrade.getBigDecimal(4));
		trade.setExecPrice(jsonTrade.getBigDecimal(5));
		
		final String orderTypeString = jsonTrade.optString(6, null);
		
		if(orderTypeString != null) {
			trade.setOrderType(BitfinexOrderType.fromString(orderTypeString));
		}
		
		trade.setOrderPrice(jsonTrade.optBigDecimal(7, null));
		trade.setMaker(jsonTrade.getInt(8) == 1 ? true : false);
		trade.setFee(jsonTrade.optBigDecimal(9, null));
		trade.setFeeCurrency(jsonTrade.optString(10, ""));

		bitfinexApiBroker.getTradeManager().updateTrade(trade);
	}
}
