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
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;

public class BitfinexTickTest {
	
	/**
	 * The double test delta
	 */
	private final static double DELTA = 0.0001;

	@Test
	public void testEquals() {
		final BitfinexTick tick1 = new BitfinexTick(210, 11, 16, 18, 10, 45);
		final BitfinexTick tick2 = new BitfinexTick(213, 12, 15, 18, 10);
		final BitfinexTick tick3 = new BitfinexTick(213, 12, 15, 18, 10);
		
		Assert.assertEquals(tick2, tick3);
		Assert.assertEquals(tick2.hashCode(), tick3.hashCode());
		Assert.assertEquals(tick3.hashCode(), tick3.hashCode());

		Assert.assertFalse(tick1.equals(tick2));
	}
	
	@Test
	public void testToString() {
		final BitfinexTick tick1 = new BitfinexTick(213, 12, 15, 18, 12);
		Assert.assertTrue(tick1.toString().length() > 10);
	}
	
	@Test
	public void testCompareTo() {
		final BitfinexTick tick1 = new BitfinexTick(210, 11, 16, 18, 10);
		final BitfinexTick tick2 = new BitfinexTick(213, 12, 15, 18, 12);
		Assert.assertTrue(tick1.compareTo(tick2) < 0);
		Assert.assertTrue(tick2.compareTo(tick1) > 0);
		Assert.assertTrue(tick1.compareTo(tick1) == 0);
	}
	
	@Test
	public void testGetter() {
		final BitfinexTick tick1 = new BitfinexTick(210, 11, 16, 18, 10, 45);
		final BitfinexTick tick2 = new BitfinexTick(213, 12, 15, 18, 12);
		
		Assert.assertEquals(210, tick1.getTimestamp());
		Assert.assertEquals(11, tick1.getOpen(), DELTA);
		Assert.assertEquals(16, tick1.getClose(), DELTA);
		Assert.assertEquals(18, tick1.getHigh(), DELTA);
		Assert.assertEquals(10, tick1.getLow(), DELTA);
		Assert.assertEquals(45, tick1.getVolume(), DELTA);

		Assert.assertEquals(BitfinexTick.INVALID_VOLUME, tick2.getVolume(), DELTA);
	}
}
