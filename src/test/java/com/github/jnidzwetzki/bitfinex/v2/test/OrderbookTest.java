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

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;

public class OrderbookTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}

	/**
	 * Test the equals method
	 */
	@Test
	public void testTradingOrderbookEquals() {
		final BitfinexOrderBookSymbol configuration1 = BitfinexSymbols.orderBook(
				BitfinexCurrencyPair.of("BCH","USD"), BitfinexOrderBookSymbol.Precision.P1, BitfinexOrderBookSymbol.Frequency.F1, 50);
		
		final BitfinexOrderBookSymbol configuration2 = BitfinexSymbols.orderBook(
				BitfinexCurrencyPair.of("BCH","USD"), BitfinexOrderBookSymbol.Precision.P1, BitfinexOrderBookSymbol.Frequency.F1, 50);
		
		final BitfinexOrderBookSymbol configuration3 = BitfinexSymbols.orderBook(
				BitfinexCurrencyPair.of("BCH","USD"), BitfinexOrderBookSymbol.Precision.P0, BitfinexOrderBookSymbol.Frequency.F1, 50);
		
		Assert.assertEquals(configuration1.hashCode(), configuration2.hashCode());
		Assert.assertEquals(configuration1, configuration2);
		Assert.assertNotEquals(configuration1, configuration3);
	}
	
	/**
	 * Test the build from JSON array
	 */
	@Test
	public void createOrderbookConfigurationFromJSON() {
		final String message = "{\"event\":\"subscribed\",\"channel\":\"book\",\"chanId\":3829,\"symbol\":\"tBTCUSD\",\"prec\":\"P0\",\"freq\":\"F0\",\"len\":\"25\",\"pair\":\"BTCUSD\"}";
		final JSONTokener tokener = new JSONTokener(message);
		final JSONObject jsonObject = new JSONObject(tokener);

		final BitfinexOrderBookSymbol configuration
			= BitfinexOrderBookSymbol.fromJSON(jsonObject);
	
		Assert.assertEquals(BitfinexCurrencyPair.of("BTC","USD"), configuration.getCurrencyPair());
		Assert.assertEquals(BitfinexOrderBookSymbol.Frequency.F0, configuration.getFrequency());
		Assert.assertEquals(BitfinexOrderBookSymbol.Precision.P0, configuration.getPrecision());
		Assert.assertEquals(25, (int) configuration.getPricePoints());
	}

}
