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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class BitfinexCurrencyTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}
	
	@Test
	public void testOf1() {
		final BitfinexCurrencyPair currency1 = BitfinexCurrencyPair.of("ETH", "USD");
		Assert.assertTrue(currency1 != null);
		
		// Reference equals
		final BitfinexCurrencyPair currency2 = BitfinexCurrencyPair.of("ETH", "USD");
		Assert.assertTrue(currency1 == currency2);
		
		// Symbol
		Assert.assertEquals("tETHUSD", currency1.toBitfinexString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testOf2() {
		final BitfinexCurrencyPair currency = BitfinexCurrencyPair.of("USD", "XYZ");
		Assert.assertTrue(currency != null);
	}
		
	@Test
	public void testRegister1() {
		final BitfinexCurrencyPair currency1 = BitfinexCurrencyPair.register("USD", "XYZ", 1.4);
		Assert.assertTrue(currency1 != null);
		
		// Reference equals
		final BitfinexCurrencyPair currency2 = BitfinexCurrencyPair.of("USD", "XYZ");
		Assert.assertTrue(currency1 == currency2);
		Assert.assertEquals(1.4, currency1.getMinimumOrderSize(), 0.0001);
		
		// Symbol
		Assert.assertEquals("tUSDXYZ", currency1.toBitfinexString());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRegister2() {
		final BitfinexCurrencyPair currency1 = BitfinexCurrencyPair.register("ETH", "USD", 1.4);
		Assert.assertTrue(currency1 != null);
	}
	
}
