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
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexConnectionFeature;
import com.github.jnidzwetzki.bitfinex.v2.SequenceNumberAuditor;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.Timeframe;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;

public class IntegrationTest {
	
	/**
	 * Try to fetch wallets on an unauthenticated connection
	 */
	@Test
	public void testWalletsOnUnauthClient() throws APIException {
		
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();

		try {
			bitfinexClient.connect();
			Assert.assertFalse(bitfinexClient.isAuthenticated());
			
			try {
				bitfinexClient.getWalletManager().getWallets();

				// Should not happen
				Assert.assertTrue(false);
			} catch (APIException e) {
				return;
			}
		
		} catch (Exception e) {
			
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test the orderbook stream
	 */
	@Test(timeout=10000)
	public void testOrderbookStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			bitfinexClient.connect();
			final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
					BitfinexCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);
			
			final OrderbookManager orderbookManager = bitfinexClient.getOrderbookManager();
			
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
				Assert.assertTrue(o.getAmount().doubleValue() != 0);
				Assert.assertTrue(o.getPrice().doubleValue() != 0);
				Assert.assertTrue(o.getCount().doubleValue() != 0);
				Assert.assertTrue(o.toString().length() > 0);
				latch.countDown();
			};
			
			orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
			orderbookManager.subscribeOrderbook(orderbookConfiguration);
			latch.await();

			orderbookManager.unsubscribeOrderbook(orderbookConfiguration);
			
			Assert.assertTrue(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));
			Assert.assertFalse(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test the raw orderbook stream
	 */
	@Test(timeout=10000)
	public void testRawOrderbookStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 20 callbacks
		final CountDownLatch latch = new CountDownLatch(20);
		try {
			bitfinexClient.connect();
			final RawOrderbookConfiguration orderbookConfiguration = new RawOrderbookConfiguration(
					BitfinexCurrencyPair.BTC_USD);
			
			final RawOrderbookManager rawOrderbookManager = bitfinexClient.getRawOrderbookManager();
			
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback = (c, o) -> {
				Assert.assertTrue(o.getAmount().doubleValue() != 0);
				Assert.assertTrue(o.getPrice().doubleValue() != 0);
				Assert.assertTrue(o.getOrderId() >= 0);
				Assert.assertTrue(o.toString().length() > 0);
				latch.countDown();
			};
			
			rawOrderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
			rawOrderbookManager.subscribeOrderbook(orderbookConfiguration);
			latch.await();

			rawOrderbookManager.unsubscribeOrderbook(orderbookConfiguration);
			
			Assert.assertTrue(rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));
			Assert.assertFalse(rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test the candle stream
	 */
	@Test(timeout=10000)
	public void testCandleStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			bitfinexClient.connect();
			final BitfinexCandlestickSymbol symbol = new BitfinexCandlestickSymbol(
					BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);
			
			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();
			
			final BiConsumer<BitfinexCandlestickSymbol, BitfinexTick> callback = (c, o) -> {
				latch.countDown();
			};
			
			orderbookManager.registerCandlestickCallback(symbol, callback);
			orderbookManager.subscribeCandles(symbol);
			latch.await();

			orderbookManager.unsubscribeCandles(symbol);
			
			Assert.assertTrue(orderbookManager.removeCandlestickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeCandlestickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test executed trades stream
	 */
	@Test(timeout=60000)
	public void testExecutedTradesStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		try {
			bitfinexClient.connect();
			final BitfinexExecutedTradeSymbol symbol = new BitfinexExecutedTradeSymbol(BitfinexCurrencyPair.BTC_USD);
			
			final QuoteManager executedTradeManager = bitfinexClient.getQuoteManager();
			
			final BiConsumer<BitfinexExecutedTradeSymbol, ExecutedTrade> callback = (c, o) -> {
				latch.countDown();
			};
			
			executedTradeManager.registerExecutedTradeCallback(symbol, callback);
			executedTradeManager.subscribeExecutedTrades(symbol);
			latch.await();

			executedTradeManager.unsubscribeExecutedTrades(symbol);

			Assert.assertTrue(executedTradeManager.removeExecutedTradeCallback(symbol, callback));
			Assert.assertFalse(executedTradeManager.removeExecutedTradeCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	
	/**
	 * Test the tick stream
	 */
	@Test(timeout=60000)
	public void testTickerStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		try {
			bitfinexClient.connect();
			final BitfinexTickerSymbol symbol = new BitfinexTickerSymbol(BitfinexCurrencyPair.BTC_USD);
			
			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();
			
			final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback = (c, o) -> {
				latch.countDown();
			};
			
			orderbookManager.registerTickCallback(symbol, callback);
			orderbookManager.subscribeTicker(symbol);
			latch.await();
			Assert.assertTrue(bitfinexClient.isTickerActive(symbol));

			orderbookManager.unsubscribeTicker(symbol);
			Assert.assertFalse(bitfinexClient.isTickerActive(symbol));

			Assert.assertTrue(orderbookManager.removeTickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeTickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test auth failed
	 * @throws APIException 
	 */
	@Test(expected=APIException.class, timeout=10000)
	public void testAuthFailed() throws APIException {
		final String KEY = "key";
		final String SECRET = "secret";
		
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker(KEY, SECRET);
		Assert.assertEquals(KEY, bitfinexClient.getApiKey());
		Assert.assertEquals(SECRET, bitfinexClient.getApiSecret());
		
		Assert.assertFalse(bitfinexClient.isAuthenticated());
		
		bitfinexClient.connect();
		
		// Should not be reached
		Assert.assertTrue(false);
		bitfinexClient.close();
	}
	
	/**
	 * Test the session reconnect
	 * @throws APIException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testReconnect() throws APIException, InterruptedException {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
		bitfinexClient.connect();
		
		final BitfinexTickerSymbol symbol = new BitfinexTickerSymbol(BitfinexCurrencyPair.BTC_USD);

		final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();
		
		orderbookManager.subscribeTicker(symbol);
		Thread.sleep(1000);
		bitfinexClient.reconnect();
		
		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		
		final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback = (c, o) -> {
			latch.countDown();
		};
		
		orderbookManager.registerTickCallback(symbol, callback);
		latch.await();
		Assert.assertTrue(bitfinexClient.isTickerActive(symbol));

		orderbookManager.unsubscribeTicker(symbol);
		Assert.assertFalse(bitfinexClient.isTickerActive(symbol));
		
		bitfinexClient.close();
	}
	
	/**
	 * Test the sequencing feature
	 * @throws APIException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testSequencing() throws APIException, InterruptedException {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
		bitfinexClient.connect();
		
		final SequenceNumberAuditor sequenceNumberAuditor = bitfinexClient.getSequenceNumberAuditor();
		Assert.assertEquals(-1, sequenceNumberAuditor.getPrivateSequence());
		Assert.assertEquals(-1, sequenceNumberAuditor.getPublicSequence());
		Assert.assertEquals(SequenceNumberAuditor.ErrorPolicy.LOG_ONLY, sequenceNumberAuditor.getErrorPolicy());

		final ConnectionFeatureManager cfManager = bitfinexClient.getConnectionFeatureManager();
		Assert.assertEquals(0, cfManager.getActiveConnectionFeatures());
		Assert.assertFalse(cfManager.isConnectionFeatureEnabled(BitfinexConnectionFeature.SEQ_ALL));
		cfManager.enableConnectionFeature(BitfinexConnectionFeature.SEQ_ALL);
		Thread.sleep(1000);
		Assert.assertTrue(cfManager.isConnectionFeatureActive(BitfinexConnectionFeature.SEQ_ALL));
		Assert.assertEquals(BitfinexConnectionFeature.SEQ_ALL.getFeatureFlag(), cfManager.getActiveConnectionFeatures());

		// Register some ticket to get some sequence numbers
		final BitfinexTickerSymbol symbol1 = new BitfinexTickerSymbol(BitfinexCurrencyPair.BTC_USD);
		final BitfinexTickerSymbol symbol2 = new BitfinexTickerSymbol(BitfinexCurrencyPair.BTC_EUR);

		final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();
		
		orderbookManager.subscribeTicker(symbol1);
		orderbookManager.subscribeTicker(symbol2);

		Thread.sleep(1000);
		
		cfManager.disableConnectionFeature(BitfinexConnectionFeature.SEQ_ALL);
		Assert.assertFalse(cfManager.isConnectionFeatureEnabled(BitfinexConnectionFeature.SEQ_ALL));
		Thread.sleep(2000);
		Assert.assertEquals(0, cfManager.getActiveConnectionFeatures());
		Assert.assertFalse(cfManager.isConnectionFeatureActive(BitfinexConnectionFeature.SEQ_ALL));

		Assert.assertEquals(-1, sequenceNumberAuditor.getPrivateSequence());
		Assert.assertTrue(sequenceNumberAuditor.getPublicSequence() > 1);

		bitfinexClient.close();
	}
}
