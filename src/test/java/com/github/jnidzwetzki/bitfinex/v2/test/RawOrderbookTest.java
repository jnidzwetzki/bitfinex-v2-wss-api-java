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
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;

public class RawOrderbookTest {

	/**
	 * Test the equals method
	 */
	@Test
	public void testTradingOrderbookEquals() {
		BitfinexOrderBookSymbol configuration1 = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("BCH","USD"));
		BitfinexOrderBookSymbol configuration2 = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("BCH","USD"));
		BitfinexOrderBookSymbol configuration3 = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("AVT","BTC"));
		
		Assert.assertEquals(configuration1.hashCode(), configuration2.hashCode());
		Assert.assertEquals(configuration1, configuration2);
		Assert.assertFalse(configuration1.equals(configuration3));
	}
	
	/**
	 * Test the build from JSON array
	 */
	@Test
	public void createBitfinexOrderBookSymbolFromJSON() {
		final String message = "{\"event\":\"subscribed\",\"channel\":\"book\",\"chanId\":3829,\"symbol\":\"tBTCUSD\",\"prec\":\"R0\",\"pair\":\"BTCUSD\"}";
		final JSONTokener tokener = new JSONTokener(message);
		final JSONObject jsonObject = new JSONObject(tokener);

		final BitfinexOrderBookSymbol configuration
			= BitfinexOrderBookSymbol.fromJSON(jsonObject);
	
		Assert.assertEquals(BitfinexCurrencyPair.of("BTC","USD"), configuration.getCurrencyPair());
	}
}
