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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;

public class BitfinexTickTest {
	
	/**
	 * The double test delta
	 */
	private final static double DELTA = 0.0001;

	@Test
	public void testEquals() {
		final BitfinexCandle tick1 = new BitfinexCandle(210, 
				new BigDecimal(11),
				new BigDecimal(16), 
				new BigDecimal(18), 
				new BigDecimal(10), 
				new BigDecimal(45));
		
		final BitfinexCandle tick2 = new BitfinexCandle(213, 
				new BigDecimal(12),
				new BigDecimal(15), 
				new BigDecimal(18), 
				new BigDecimal(10));
		
		final BitfinexCandle tick3 = new BitfinexCandle(213, 
				new BigDecimal(12),
				new BigDecimal(15), 
				new BigDecimal(18), 
				new BigDecimal(10));
		
		Assert.assertEquals(tick2, tick3);
		Assert.assertEquals(tick2.hashCode(), tick3.hashCode());
		Assert.assertEquals(tick3.hashCode(), tick3.hashCode());

		Assert.assertFalse(tick1.equals(tick2));
	}
	
	@Test
	public void testToString() {
		final BitfinexCandle tick1 = new BitfinexCandle(213, 12d, 15d, 18d, 12d, 100d);
		Assert.assertTrue(tick1.toString().length() > 10);
	}
	
	@Test
	public void testCompareTo() {
		final BitfinexCandle tick1 = new BitfinexCandle(210, 11d, 16d, 18d, 10d, 100d);
		final BitfinexCandle tick2 = new BitfinexCandle(213, 12d, 15d, 18d, 12d, 100d);
		Assert.assertTrue(tick1.compareTo(tick2) < 0);
		Assert.assertTrue(tick2.compareTo(tick1) > 0);
		Assert.assertTrue(tick1.compareTo(tick1) == 0);
	}
	
	@Test
	public void testGetter() {
		final BitfinexCandle tick1 = new BitfinexCandle(210, 11, 16, 18, 10, 45);
		
		final BitfinexCandle tick2 = new BitfinexCandle(210, 
				new BigDecimal(11),
				new BigDecimal(16), 
				new BigDecimal(18), 
				new BigDecimal(10));		
		
		Assert.assertEquals(210, tick1.getTimestamp());
		Assert.assertEquals(11, tick1.getOpen().doubleValue(), DELTA);
		Assert.assertEquals(16, tick1.getClose().doubleValue(), DELTA);
		Assert.assertEquals(18, tick1.getHigh().doubleValue(), DELTA);
		Assert.assertEquals(10, tick1.getLow().doubleValue(), DELTA);
		Assert.assertEquals(45, tick1.getVolume().get().doubleValue(), DELTA);

		Assert.assertFalse(tick2.getVolume().isPresent());
	}
}
