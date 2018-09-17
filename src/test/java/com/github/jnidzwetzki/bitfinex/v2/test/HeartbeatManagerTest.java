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

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.HeartbeatThread;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.WebsocketClientEndpoint;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.HeartbeatHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;

public class HeartbeatManagerTest {
	
	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
		}
		BitfinexCurrencyPair.registerDefaults();	
	}

	@Test(timeout=30000)
	public void testConnectWhenDisconnected() throws Exception {

		// Events
		// * Disconnect websocket from heartbeat
		// * Disconnect from bitfinex API reconnect method
		// * Reconnect on bitfinex api
		final CountDownLatch connectLatch = new CountDownLatch(3);

		// Count down the latch on method call
		final Answer<Void> answer = invocation -> {
				connectLatch.countDown();
				return null;
		};

		final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);

		final WebsocketClientEndpoint websocketClientEndpoint = Mockito.mock(WebsocketClientEndpoint.class);
		Mockito.when(websocketClientEndpoint.isConnected()).thenReturn(connectLatch.getCount() == 0);
		AtomicLong heartbeat = new AtomicLong(0);
		final HeartbeatThread heartbeatThreadRunnable = new HeartbeatThread(bitfinexApiBroker, websocketClientEndpoint, heartbeat::get);

		Mockito.doAnswer(answer).when(bitfinexApiBroker).reconnect();
		Mockito.doAnswer(answer).when(websocketClientEndpoint).close();

		final Thread heartbeatThread = new Thread(heartbeatThreadRunnable);

		try {
			heartbeatThread.start();
			connectLatch.await();
		} catch (Exception e) {
			// Should not happen
			throw e;
		} finally {
			heartbeatThread.interrupt();
		}
	}

	/**
	 * Test the heartbeart handler
	 * @throws BitfinexClientException
	 */
	@Test
	public void testHeartbeatHandler() throws BitfinexClientException, InterruptedException {
		final HeartbeatHandler handler = new HeartbeatHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		long heartbeat = System.currentTimeMillis();
		handler.onHeartbeatEvent(timestamp -> Assert.assertTrue(timestamp > heartbeat));
		Thread.sleep(50);
		handler.handleChannelData(null, null);
	}

	/**
	 * Test the ticker freshness
	 */
	@Test
	public void testTickerFreshness1() {
		final HashMap<BitfinexStreamSymbol, Long> heartbeatValues = new HashMap<>();
		Assert.assertTrue(HeartbeatThread.checkTickerFreshness(heartbeatValues));
	}

	/**
	 * Test the ticker freshness
	 */
	@Test
	public void testTickerFreshness2() {
		final HashMap<BitfinexStreamSymbol, Long> heartbeatValues = new HashMap<>();
		heartbeatValues.put(BitfinexSymbols.ticker(BitfinexCurrencyPair.of("AGI","ETH")), System.currentTimeMillis());
		Assert.assertTrue(HeartbeatThread.checkTickerFreshness(heartbeatValues));
	}

	/**
	 * Test the ticker freshness
	 */
	@Test
	public void testTickerFreshness3() {
		final HashMap<BitfinexStreamSymbol, Long> heartbeatValues = new HashMap<>();
		long outdatedTime = System.currentTimeMillis() - HeartbeatThread.TICKER_TIMEOUT - 10;
		heartbeatValues.put(BitfinexSymbols.ticker(BitfinexCurrencyPair.of("AGI","ETH")), outdatedTime);
		Assert.assertFalse(HeartbeatThread.checkTickerFreshness(heartbeatValues));
	}
}
