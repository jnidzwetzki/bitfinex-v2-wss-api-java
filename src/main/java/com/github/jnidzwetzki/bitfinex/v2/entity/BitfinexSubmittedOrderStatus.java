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

public enum BitfinexSubmittedOrderStatus {
	
	ACTIVE("ACTIVE"),
	EXECUTED("EXECUTED"),
	PARTIALLY_FILLED("PARTIALLY FILLED"),
	POSTONLY_CANCELED("POSTONLY CANCELED"),
	CANCELED("CANCELED"),
	ERROR("ERROR");

	private String bitfinexString;
	
	BitfinexSubmittedOrderStatus(final String bitfinexString) {
		this.bitfinexString = bitfinexString;
	}
	
	public static BitfinexSubmittedOrderStatus fromString(final String string) {
		Objects.requireNonNull(string);
		for (BitfinexSubmittedOrderStatus state : BitfinexSubmittedOrderStatus.values()) {
			if (string.startsWith(state.bitfinexString)) {
				return state;
			}
		}

		// Handle special cases
		// Case1: INSUFFICIENT BALANCE (G1) was: ACTIVE (note:POSCLOSE), PARTIALLY FILLED
		if(string.contains(", PARTIALLY FILLED")) {
			return BitfinexSubmittedOrderStatus.PARTIALLY_FILLED;
		}
		throw new IllegalArgumentException("Unable to find order type for: " + string);
	}
}
