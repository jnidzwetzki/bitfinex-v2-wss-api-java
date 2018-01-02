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
package com.github.jnidzwetzki.bitfinex.v2.entity;

import java.util.Objects;

public enum ExchangeOrderState {
	
	STATE_ACTIVE("ACTIVE"),
	STATE_EXECUTED("EXECUTED"),
	STATE_PARTIALLY_FILLED("PARTIALLY FILLED"),
	STATE_CANCELED("CANCELED"),
	STATE_POSTONLY_CANCELED("POSTONLY CANCELED"),
	STATE_ERROR("ERROR");

	private String bitfinexString;
	
	private ExchangeOrderState(final String bitfinexString) {
		this.bitfinexString = bitfinexString;
	}
	
	public String getBitfinexString() {
		return bitfinexString;
	}
	
	public static ExchangeOrderState fromString(final String string) {
		
		Objects.requireNonNull(string);
		
		for (ExchangeOrderState state : ExchangeOrderState.values()) {
			if (string.startsWith(state.getBitfinexString())) {
				return state;
			}
		}
		
		throw new IllegalArgumentException("Unable to find order type for: " + string);
	}
}
