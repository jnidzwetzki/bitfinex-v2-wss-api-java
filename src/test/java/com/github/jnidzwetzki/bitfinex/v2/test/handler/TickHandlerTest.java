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
package com.github.jnidzwetzki.bitfinex.v2.test.handler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.TickHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;


public class TickHandlerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the parsing of one tick
	 * @throws APIException
	 * @throws InterruptedException
	 */
	@Test(timeout=20000)
	public void testTickUpdateAndNotify() throws APIException, InterruptedException {

		final String callbackValue = "[26123,41.4645776,26129,33.68138507,2931,0.2231,26129,144327.10936387,26149,13139]";
		final JSONArray jsonArray = new JSONArray(callbackValue);

		final BitfinexCurrencyPair currencyPair = BitfinexCurrencyPair.of("BTC","USD");
		final BitfinexTickerSymbol symbol = new BitfinexTickerSymbol(currencyPair);

		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker, executorService);
		Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(tickerManager);

		final CountDownLatch latch = new CountDownLatch(1);

		tickerManager.registerTickCallback(symbol, (s, c) -> {

			try {
				Assert.assertEquals(symbol, s);
				Assert.assertEquals(26123d, c.getBid().doubleValue(), DELTA);
				Assert.assertEquals(41.4645776, c.getBidSize().doubleValue(), DELTA);
				Assert.assertEquals(26129d, c.getAsk().doubleValue(), DELTA);
				Assert.assertEquals(33.68138507, c.getAskSize().doubleValue(), DELTA);
				Assert.assertEquals(2931d, c.getDailyChange().doubleValue(), DELTA);
				Assert.assertEquals(0.2231, c.getDailyChangePerc().doubleValue(), DELTA);
				Assert.assertEquals(26129d, c.getLastPrice().doubleValue(), DELTA);
				Assert.assertEquals(144327.10936387, c.getVolume().doubleValue(), DELTA);
				Assert.assertEquals(26149d, c.getHigh().doubleValue(), DELTA);
				Assert.assertEquals(13139d, c.getLow().doubleValue(), DELTA);
			} catch(Throwable e) {
				System.out.println(e);
				throw e;
			}

			latch.countDown();
		});


		Assert.assertEquals(-1, tickerManager.getHeartbeatForSymbol(symbol));

		final TickHandler tickHandler = new TickHandler();
		tickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);

		// Tick callbacks are handled async
		latch.await();

		Assert.assertTrue(tickerManager.getHeartbeatForSymbol(symbol) != -1);
	}

}
