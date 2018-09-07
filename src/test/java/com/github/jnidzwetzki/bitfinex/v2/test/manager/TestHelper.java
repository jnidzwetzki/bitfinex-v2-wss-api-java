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
package com.github.jnidzwetzki.bitfinex.v2.test.manager;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.MoreExecutors;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBrokerConfig;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;

public class TestHelper {
	
	/**
	 * The API key of the connection
	 */
	public final static String API_KEY = "abc123";
	
	/**
	 * Build a mocked bitfinex connection
	 * @return
	 */
	public static BitfinexApiBroker buildMockedBitfinexConnection() {

		final ExecutorService executorService = MoreExecutors.newDirectExecutorService();
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final BitfinexApiBrokerConfig config = Mockito.mock(BitfinexApiBrokerConfig.class);

		Mockito.when(bitfinexApiBroker.getConfiguration()).thenReturn(config);
		Mockito.when(config.getApiKey()).thenReturn(API_KEY);
		Mockito.when(bitfinexApiBroker.isAuthenticated()).thenReturn(true);
		Mockito.when(bitfinexApiBroker.getApiKeyPermissions()).thenReturn(BitfinexApiKeyPermissions.ALL_PERMISSIONS);
		Mockito.when(bitfinexApiBroker.getCallbacks()).thenReturn(new BitfinexApiCallbackRegistry());

		final OrderManager orderManager = new OrderManager(bitfinexApiBroker, executorService);
		final TradeManager tradeManager = new TradeManager(bitfinexApiBroker, executorService);
		Mockito.when(bitfinexApiBroker.getOrderManager()).thenReturn(orderManager);
		Mockito.when(bitfinexApiBroker.getTradeManager()).thenReturn(tradeManager);

		return bitfinexApiBroker;
	}
}
