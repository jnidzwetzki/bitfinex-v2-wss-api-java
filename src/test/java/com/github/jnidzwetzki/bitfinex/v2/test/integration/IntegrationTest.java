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
package com.github.jnidzwetzki.bitfinex.v2.test.integration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexConnectionFeature;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.SequenceNumberAuditor;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.FutureOperation;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class IntegrationTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}

	/**
	 * Try to fetch wallets on an unauthenticated connection
	 */
	@Test
	public void testWalletsOnUnauthClient() throws BitfinexClientException {

		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		try {
			bitfinexClient.connect();
			Assert.assertFalse(bitfinexClient.isAuthenticated());

			try {
				bitfinexClient.getWalletManager().getWallets();

				// Should not happen
				Assert.fail();
			} catch (BitfinexClientException e) {
				return;
			}

		} catch (Exception e) {

			// Should not happen
			e.printStackTrace();
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test the orderbook stream
	 */
	@Test(timeout=30000)
	public void testOrderbookStream() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			bitfinexClient.connect();
			final BitfinexOrderBookSymbol orderbookConfiguration = BitfinexSymbols.orderBook(
					BitfinexCurrencyPair.of("BTC","USD"), BitfinexOrderBookSymbol.Precision.P0, BitfinexOrderBookSymbol.Frequency.F0, 25);

			final OrderbookManager orderbookManager = bitfinexClient.getOrderbookManager();

			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback = (c, o) -> {
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
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test the raw orderbook stream
	 */
	@Test(timeout=30000)
	public void testRawOrderbookStream() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		// Await at least 20 callbacks
		final CountDownLatch latch = new CountDownLatch(20);
		try {
			bitfinexClient.connect();
			BitfinexOrderBookSymbol orderbookConfiguration = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("BTC", "USD"));

			final RawOrderbookManager rawOrderbookManager = bitfinexClient.getRawOrderbookManager();

			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback = (c, o) -> {
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
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test the candle stream
	 */
	@Test(timeout=30000)
	public void testCandleStream() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		try {
			bitfinexClient.connect();
			final List<BitfinexCandlestickSymbol> symbols = Arrays.asList(
						BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MINUTES_1),
						BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.DAY_1),
						BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MONTH_1)
						);

			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

			for(final BitfinexCandlestickSymbol symbol : symbols) {
				// Await at least 10 callbacks
				final CountDownLatch latch1 = new CountDownLatch(10);

				final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback = (c, o) -> {
					latch1.countDown();
				};

				orderbookManager.registerCandlestickCallback(symbol, callback);
				orderbookManager.subscribeCandles(symbol);
				latch1.await();

				orderbookManager.unsubscribeCandles(symbol);

				Assert.assertTrue(orderbookManager.removeCandlestickCallback(symbol, callback));
				Assert.assertFalse(orderbookManager.removeCandlestickCallback(symbol, callback));
			}
		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test executed trades stream
	 */
	@Test(timeout=60000)
	public void testExecutedTradesStream() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		try {
			bitfinexClient.connect();
			final BitfinexExecutedTradeSymbol symbol = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BTC","USD"));

			final QuoteManager executedTradeManager = bitfinexClient.getQuoteManager();

			final BiConsumer<BitfinexExecutedTradeSymbol, BitfinexExecutedTrade> callback = (c, o) -> {
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
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test unsubscribe all channels
	 */
	@Test(timeout=60000)
	public void testUnsubscrribeAllChannels() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		try {
			bitfinexClient.connect();
			final BitfinexExecutedTradeSymbol symbol1 = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BTC","USD"));
			final BitfinexExecutedTradeSymbol symbol2 = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("ETH","USD"));

			final QuoteManager quoteManager = bitfinexClient.getQuoteManager();

			final FutureOperation subscribe1 = quoteManager.subscribeExecutedTrades(symbol1);
			final FutureOperation subscribe2 = quoteManager.subscribeExecutedTrades(symbol2);
			subscribe1.waitForCompletion();
			subscribe2.waitForCompletion();

			Assert.assertEquals(3, bitfinexClient.getSubscribedChannels().size());
			bitfinexClient.unsubscribeAllChannels();
			Assert.assertTrue(bitfinexClient.getSubscribedChannels().isEmpty());
		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}


	/**
	 * Test the tick stream
	 */
	@Test(timeout=120_000)
	public void testTickerStream() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		try {
			bitfinexClient.connect();
			final BitfinexTickerSymbol symbol = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("BTC","USD"));

			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

			final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback = (c, o) -> {
				latch.countDown();
			};

			orderbookManager.registerTickCallback(symbol, callback);
			orderbookManager.subscribeTicker(symbol);
			latch.await();
			Assert.assertTrue(bitfinexClient.getSubscribedChannels().contains(symbol));

			final FutureOperation unsubscribeFuture = orderbookManager.unsubscribeTicker(symbol);
			unsubscribeFuture.waitForCompletion();
			Assert.assertFalse(bitfinexClient.getSubscribedChannels().contains(symbol));

			Assert.assertTrue(orderbookManager.removeTickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeTickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}

	/**
	 * Test auth failed
	 * @throws BitfinexClientException
	 */
	@Test(expected=BitfinexClientException.class, timeout=120_000)
	public void testAuthFailed() throws BitfinexClientException {
		final String KEY = "key";
		final String SECRET = "secret";

		BitfinexWebsocketConfiguration config = new BitfinexWebsocketConfiguration();
		config.setApiCredentials(KEY, SECRET);
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
		Assert.assertEquals(KEY, bitfinexClient.getConfiguration().getApiKey());
		Assert.assertEquals(SECRET, bitfinexClient.getConfiguration().getApiSecret());

		Assert.assertFalse(bitfinexClient.isAuthenticated());

		bitfinexClient.connect();

		// Should not be reached
		Assert.fail();
		bitfinexClient.close();
	}

	/**
	 * Test the session reconnect
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 * @throws ExecutionException 
	 */
	@Test(timeout=600000)
	public void testReconnect() throws BitfinexClientException, InterruptedException, ExecutionException {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
		bitfinexClient.connect();

		final BitfinexTickerSymbol symbol = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("BTC","USD"));

		final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

		final FutureOperation subscribeFuture = orderbookManager.subscribeTicker(symbol);
		subscribeFuture.waitForCompletion();
		
		bitfinexClient.reconnect();

		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);

		final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback = (c, o) -> {
			latch.countDown();
		};

		orderbookManager.registerTickCallback(symbol, callback);
		latch.await();
		Assert.assertTrue(bitfinexClient.getSubscribedChannels().contains(symbol));

		final FutureOperation unsubscribeFuture = orderbookManager.unsubscribeTicker(symbol);
		unsubscribeFuture.waitForCompletion();
		
		Assert.assertFalse(bitfinexClient.getSubscribedChannels().contains(symbol));

		bitfinexClient.close();
	}

	/**
	 * Test the sequencing feature
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testSequencing() 
			throws BitfinexClientException, InterruptedException, ExecutionException, TimeoutException {
		
		final SequenceNumberAuditor sequenceNumberAuditor = new SequenceNumberAuditor();
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), sequenceNumberAuditor);
		bitfinexClient.connect();

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
		final BitfinexTickerSymbol symbol1 = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("BTC","USD"));
		final BitfinexTickerSymbol symbol2 = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("ETH","USD"));
		final BitfinexTickerSymbol symbol3 = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("EOS","USD"));
		final BitfinexTickerSymbol symbol4 = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("IOS","USD"));
		final BitfinexTickerSymbol symbol5 = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("NEO","USD"));

		final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

		final FutureOperation operation1 = orderbookManager.subscribeTicker(symbol1);
		final FutureOperation operation2 = orderbookManager.subscribeTicker(symbol2);
		final FutureOperation operation3 = orderbookManager.subscribeTicker(symbol3);
		final FutureOperation operation4 = orderbookManager.subscribeTicker(symbol4);
		final FutureOperation operation5 = orderbookManager.subscribeTicker(symbol5);

		final List<FutureOperation> operations = Arrays.asList(operation1, operation2, operation3, operation4, operation5);
		
		for(final FutureOperation operation : operations) {
			operation.waitForCompletion(100, TimeUnit.SECONDS);
		}

		cfManager.disableConnectionFeature(BitfinexConnectionFeature.SEQ_ALL);
		Assert.assertFalse(cfManager.isConnectionFeatureEnabled(BitfinexConnectionFeature.SEQ_ALL));
		Thread.sleep(2000);
		Assert.assertEquals(0, cfManager.getActiveConnectionFeatures());
		Assert.assertFalse(cfManager.isConnectionFeatureActive(BitfinexConnectionFeature.SEQ_ALL));

		Assert.assertEquals(-1, sequenceNumberAuditor.getPrivateSequence());
		Assert.assertTrue(sequenceNumberAuditor.getPublicSequence() > 1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		bitfinexClient.close();
	}

	/**
	 * Test the error callback
	 */
	@Test(timeout=30000)
	public void testErrorCallback() {
		final BitfinexWebsocketClient bitfinexClient = new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(), new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());

		// Await at least 5 callbacks
		final CountDownLatch latch = new CountDownLatch(5);
		try {
			bitfinexClient.connect();
			final BitfinexCandlestickSymbol symbol = BitfinexSymbols.candlesticks(
					BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MINUTES_1);

			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

			final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback = (c, o) -> {
				latch.countDown();
			};

			// 1st subscribe call
			orderbookManager.registerCandlestickCallback(symbol, callback);
			orderbookManager.subscribeCandles(symbol);

			// 2nd subscribe call
			orderbookManager.subscribeCandles(symbol);

			latch.await();

			orderbookManager.unsubscribeCandles(symbol);

			Assert.assertTrue(orderbookManager.removeCandlestickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeCandlestickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.fail();
		} finally {
			bitfinexClient.close();
		}
	}
}
