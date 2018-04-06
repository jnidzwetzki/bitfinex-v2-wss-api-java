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
package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.TradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;

public class TradeManagerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	
	/**
	 * The API key of the connection
	 */
	private final static String API_KEY = "abc123";

	/**
	 * Test the trade channel handler
	 * @throws APIException 
	 * @throws InterruptedException 
	 */
	@Test(timeout=10000)
	public void testTradeChannelHandler1() throws APIException, InterruptedException {
		final String jsonString = "[0,\"te\",[106655593,\"tBTCUSD\",1512247319827,5691690918,-0.002,10894,null,null,-1]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		final TradeHandler tradeHandler = new TradeHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		bitfinexApiBroker.getTradeManager().registerCallback((t) -> {
			
			try {
				Assert.assertTrue(t.isExecuted());
				Assert.assertEquals(106655593, t.getId());
				Assert.assertEquals(BitfinexCurrencyPair.BTC_USD, t.getCurrency());
				Assert.assertEquals(1512247319827l, t.getMtsCreate());
				Assert.assertEquals(5691690918l, t.getOrderId());
				Assert.assertEquals(-0.002, t.getExecAmount().doubleValue(), DELTA);
				Assert.assertEquals(10894, t.getExecPrice().doubleValue(), DELTA);

				Assert.assertTrue(t.toString().length() > 0);
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
			
			latch.countDown();
		});
		
		tradeHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		
		latch.await();
	}
	
	/**
	 * Test the trade channel handler
	 * @throws APIException 
	 * @throws InterruptedException 
	 */
	@Test(timeout=10000)
	public void testTradeChannelHandler2() throws APIException, InterruptedException {
		final String jsonString = "[0,\"tu\",[106655593,\"tBTCUSD\",1512247319827,5691690918,-0.002,10894,\"EXCHANGE MARKET\",10894,-1,-0.0392184,\"USD\"]]";
	
		final JSONArray jsonArray = new JSONArray(jsonString);
		final TradeHandler tradeHandler = new TradeHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		bitfinexApiBroker.getTradeManager().registerCallback((t) -> {
			
			try {
				Assert.assertFalse(t.isExecuted());
				Assert.assertEquals(106655593, t.getId());
				Assert.assertEquals(BitfinexCurrencyPair.BTC_USD, t.getCurrency());
				Assert.assertEquals(1512247319827l, t.getMtsCreate());
				Assert.assertEquals(5691690918l, t.getOrderId());
				Assert.assertEquals(-0.002, t.getExecAmount().doubleValue(), DELTA);
				Assert.assertEquals(10894, t.getExecPrice().doubleValue(), DELTA);
				Assert.assertEquals(BitfinexOrderType.EXCHANGE_MARKET, t.getOrderType());
				Assert.assertEquals(10894, t.getOrderPrice().doubleValue(), DELTA);
				Assert.assertFalse(t.isMaker());
				Assert.assertEquals(-0.0392184, t.getFee().doubleValue(), DELTA);
				Assert.assertEquals("USD", t.getFeeCurrency());
				Assert.assertTrue(t.toString().length() > 0);
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
			
			latch.countDown();
		});
		
		tradeHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		
		latch.await();
	}
	
	/**
	 * Build a mocked bitfinex connection
	 * @return
	 */
	private BitfinexApiBroker buildMockedBitfinexConnection() {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		Mockito.when(bitfinexApiBroker.getApiKey()).thenReturn(API_KEY);
		Mockito.when(bitfinexApiBroker.isAuthenticated()).thenReturn(true);
		Mockito.when(bitfinexApiBroker.getCapabilities()).thenReturn(ConnectionCapabilities.ALL_CAPABILITIES);
		
		final TradeManager tradeManager = new TradeManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getTradeManager()).thenReturn(tradeManager);
		
		return bitfinexApiBroker;
	}
	
}
