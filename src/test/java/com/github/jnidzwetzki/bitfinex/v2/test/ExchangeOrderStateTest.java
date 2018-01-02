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

import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class ExchangeOrderStateTest {

	@Test
	public void testStateFromString() {
		Assert.assertEquals(ExchangeOrderState.STATE_ACTIVE, ExchangeOrderState.fromString("ACTIVE"));
		Assert.assertEquals(ExchangeOrderState.STATE_EXECUTED, ExchangeOrderState.fromString("EXECUTED @ 18867.0(-0.01)"));
		Assert.assertEquals(ExchangeOrderState.STATE_CANCELED, ExchangeOrderState.fromString("CANCELED"));
		Assert.assertEquals(ExchangeOrderState.STATE_PARTIALLY_FILLED, ExchangeOrderState.fromString("PARTIALLY FILLED"));
		Assert.assertEquals(ExchangeOrderState.STATE_POSTONLY_CANCELED, ExchangeOrderState.fromString("POSTONLY CANCELED"));		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStateFromStringInvalid() {
		ExchangeOrderState.fromString("ABC");
	}
}
