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
package com.github.jnidzwetzki.bitfinex.v2.symbol;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class BitfinexExecutedTradeSymbol implements BitfinexStreamSymbol {
	
	/**
	 * The currency pair
	 */
	private final BitfinexCurrencyPair bitfinexCurrencyPair;

	public BitfinexExecutedTradeSymbol(final BitfinexCurrencyPair bitfinexCurrencyPair) {
		this.bitfinexCurrencyPair = bitfinexCurrencyPair;
	}

	@Override
	public String toString() {
		return "BitfinexExecutedTradeSymbol [bitfinexCurrencyPair=" + bitfinexCurrencyPair + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitfinexCurrencyPair == null) ? 0 : bitfinexCurrencyPair.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfinexExecutedTradeSymbol other = (BitfinexExecutedTradeSymbol) obj;
		if (bitfinexCurrencyPair != other.bitfinexCurrencyPair)
			return false;
		return true;
	}
	
	/**
	 * Build from bitfinex string
	 * @param symbol
	 * @return
	 */
	public static BitfinexExecutedTradeSymbol fromBitfinexString(final String symbol) {
		final BitfinexCurrencyPair bitfinexCurrencyPair = BitfinexCurrencyPair.fromSymbolString(symbol);
		return new BitfinexExecutedTradeSymbol(bitfinexCurrencyPair);
	}

	/**
	 * Get the currency pair
	 * @return
	 */
	public BitfinexCurrencyPair getBitfinexCurrencyPair() {
		return bitfinexCurrencyPair;
	}
	
}
