package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradeOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradingOrderbookManager;

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
				bitfinexClient.getWallets();

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
	 * Test the trading orderbook stream
	 */
	@Test(timeout=10000)
	public void testTradingOrderbook() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			bitfinexClient.connect();
			final TradeOrderbookConfiguration orderbookConfiguration = new TradeOrderbookConfiguration(
					BitfinexCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);
			
			final TradingOrderbookManager orderbookManager = bitfinexClient.getTradingOrderbookManager();
			
			final BiConsumer<TradeOrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
				latch.countDown();
			};
			
			orderbookManager.registerTradingOrderbookCallback(orderbookConfiguration, callback);
			orderbookManager.subscribeOrderbook(orderbookConfiguration);
			latch.await();

			orderbookManager.unsubscribeOrderbook(orderbookConfiguration);
			
		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
}
