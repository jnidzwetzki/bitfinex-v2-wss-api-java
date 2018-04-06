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
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ExecutedTradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;

public class ExecutedTradesHandlerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the parsing of one executed trade
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test(timeout=10000)
	public void testExecutedTradesUpdateAndNotify() throws APIException, InterruptedException {
		
		final String callbackValue = "[190631057,1518037080162,0.007,8175.9]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final BitfinexExecutedTradeSymbol symbol 
			= new BitfinexExecutedTradeSymbol(BitfinexCurrencyPair.BTC_USD);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final QuoteManager quoteManager = new QuoteManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(quoteManager);

		final CountDownLatch latch = new CountDownLatch(1);

		quoteManager.registerExecutedTradeCallback(symbol, (s, c) -> {
			try {
				Assert.assertEquals(symbol, s);
				Assert.assertEquals(190631057, c.getId());
				Assert.assertEquals(1518037080162l, c.getTimestamp());
				Assert.assertEquals(0.007, c.getAmount().doubleValue(), DELTA);
				Assert.assertEquals(8175.9, c.getPrice().doubleValue(), DELTA);
				latch.countDown();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
						
		final ExecutedTradeHandler handler = new ExecutedTradeHandler();
		handler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		latch.await();
	}
			
	/**
	 * Test the parsing of a executed trades snapshot
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test(timeout=10000)
	public void testExecutedTradesSnapshotUpdateAndNotify() throws APIException, InterruptedException {
		
		final String callbackValue = "[[190631057,1518037080162,0.007,8175.9],[190631052,1518037080110,-0.25,8175.8]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);

		final BitfinexExecutedTradeSymbol symbol 
			= new BitfinexExecutedTradeSymbol(BitfinexCurrencyPair.BTC_USD);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final QuoteManager quoteManager = new QuoteManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(quoteManager);

		final CountDownLatch latch = new CountDownLatch(2);

		quoteManager.registerExecutedTradeCallback(symbol, (s, c) -> {
			try {
				Assert.assertEquals(symbol, s);
				if(c.getId() == 190631057) {
					Assert.assertEquals(190631057, c.getId());
					Assert.assertEquals(1518037080162l, c.getTimestamp());
					Assert.assertEquals(0.007, c.getAmount().doubleValue(), DELTA);
					Assert.assertEquals(8175.9, c.getPrice().doubleValue(), DELTA);
				} else if(c.getId() == 190631052) {
					Assert.assertEquals(190631052, c.getId());
					Assert.assertEquals(1518037080110l, c.getTimestamp());
					Assert.assertEquals(-0.25, c.getAmount().doubleValue(), DELTA);
					Assert.assertEquals(8175.8, c.getPrice().doubleValue(), DELTA);
				} else {
					throw new IllegalArgumentException("Illegal call, expected 2 trades");
				}
				latch.countDown();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
						
		final ExecutedTradeHandler handler = new ExecutedTradeHandler();
		handler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		latch.await();
	}
	
}
