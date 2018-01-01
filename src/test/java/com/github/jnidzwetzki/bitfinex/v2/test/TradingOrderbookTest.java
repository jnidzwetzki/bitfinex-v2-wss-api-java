package com.github.jnidzwetzki.bitfinex.v2.test;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradingOrderbookConfiguration;

public class TradingOrderbookTest {

	/**
	 * Test the equals method
	 */
	@Test
	public void testTradingOrderbookEquals() {
		final TradingOrderbookConfiguration configuration1 = new TradingOrderbookConfiguration(
				BitfinexCurrencyPair.BCH_USD, OrderBookPrecision.P1, OrderBookFrequency.F1, 50);
		
		final TradingOrderbookConfiguration configuration2 = new TradingOrderbookConfiguration(
				BitfinexCurrencyPair.BCH_USD, OrderBookPrecision.P1, OrderBookFrequency.F1, 50);
		
		final TradingOrderbookConfiguration configuration3 = new TradingOrderbookConfiguration(
				BitfinexCurrencyPair.BCH_USD, OrderBookPrecision.P0, OrderBookFrequency.F1, 50);
		
		Assert.assertEquals(configuration1.hashCode(), configuration2.hashCode());
		Assert.assertEquals(configuration1, configuration2);
		Assert.assertFalse(configuration1.equals(configuration3));
	}
	
	/**
	 * Test the build from JSON array
	 */
	@Test
	public void createTradingOrderbookConfigurationFromJSON() {
		final String message = "{\"event\":\"subscribed\",\"channel\":\"book\",\"chanId\":3829,\"symbol\":\"tBTCUSD\",\"prec\":\"P0\",\"freq\":\"F0\",\"len\":\"25\",\"pair\":\"BTCUSD\"}";
		final JSONTokener tokener = new JSONTokener(message);
		final JSONObject jsonObject = new JSONObject(tokener);

		final TradingOrderbookConfiguration configuration 
			= TradingOrderbookConfiguration.fromJSON(jsonObject);
	
		Assert.assertEquals(BitfinexCurrencyPair.BTC_USD, configuration.getCurrencyPair());
		Assert.assertEquals(OrderBookFrequency.F0, configuration.getOrderBookFrequency());
		Assert.assertEquals(OrderBookPrecision.P0, configuration.getOrderBookPrecision());
		Assert.assertEquals(25, configuration.getPricePoints());
	}
	
	/**
	 * Test the symbol string encoding and decoding
	 */
	@Test
	public void fromAndToSymbolString() {
		final TradingOrderbookConfiguration configuration1 = new TradingOrderbookConfiguration(
				BitfinexCurrencyPair.BCH_USD, OrderBookPrecision.P1, OrderBookFrequency.F1, 50);
		
		final JSONObject json = configuration1.toJSON();
		
		final TradingOrderbookConfiguration configuration2 = TradingOrderbookConfiguration.fromJSON(json);
		
		Assert.assertEquals(configuration1, configuration2);
	}
}
