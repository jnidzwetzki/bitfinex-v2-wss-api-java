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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.concurrent.ExecutorService;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;

public class TradeManager extends SimpleCallbackManager<BitfinexMyExecutedTrade> {

	public TradeManager(final BitfinexApiBroker bitfinexApiBroker, final ExecutorService executorService) {
		super(executorService, bitfinexApiBroker);
		bitfinexApiBroker.getCallbacks().onTradeEvent(this::updateTrade);
	}
	
	/**
	 * Update a exchange order
	 * @param trade
	 */
	public void updateTrade(final BitfinexAccountSymbol account, final BitfinexMyExecutedTrade trade) {
		trade.setApiKey(bitfinexApiBroker.getConfiguration().getApiKey());
		notifyCallbacks(trade);
	}
	
}
	
