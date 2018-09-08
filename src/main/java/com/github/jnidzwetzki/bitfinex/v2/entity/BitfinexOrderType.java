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

public enum BitfinexOrderType {

	MARKET("MARKET"), 
	EXCHANGE_MARKET("EXCHANGE MARKET"), 
	LIMIT("LIMIT"), 
	EXCHANGE_LIMIT("EXCHANGE LIMIT"), 
	STOP("STOP"), 
	EXCHANGE_STOP("EXCHANGE STOP"), 
	TRAILING_STOP("TRAILING STOP"), 
	EXCHANGE_TRAILING_STOP("EXCHANGE TRAILING STOP"), 
	FOK("FOK"), 
	EXCHANGE_FOK("EXCHANGE FOK"), 
	STOP_LIMIT("STOP LIMIT"), 
	EXCHANGE_STOP_LIMIT("EXCHANGE STOP LIMIT");

	private final String bifinexString;

	BitfinexOrderType(final String bifinexString) {
		this.bifinexString = bifinexString;
	}

	public String getBifinexString() {
		return bifinexString;
	}

	public static BitfinexOrderType fromBifinexString(String value) {
		for (BitfinexOrderType orderType : BitfinexOrderType.values()) {
			if (orderType.getBifinexString().equalsIgnoreCase(value)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException("Unable to find order type for: " + value);
	}
}
