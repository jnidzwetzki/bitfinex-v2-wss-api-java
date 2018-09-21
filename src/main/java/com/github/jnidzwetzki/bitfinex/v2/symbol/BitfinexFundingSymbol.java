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

import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexFundingCurrency;

public class BitfinexFundingSymbol implements BitfinexStreamSymbol {
	
	/**
	 * The currency pair
	 */
	private final BitfinexFundingCurrency currency;

	BitfinexFundingSymbol(final BitfinexFundingCurrency currency) {
		this.currency = currency;
	}

	/**
	 * Build from bitfinex string
	 * @param symbol
	 * @return
	 */
	public static BitfinexFundingSymbol fromBitfinexString(final String symbol) {
		final BitfinexFundingCurrency bitfinexCurrency = BitfinexFundingCurrency.fromSymbolString(symbol);
		return BitfinexSymbols.funding(bitfinexCurrency);
	}

	/**
	 * Get the funding currency
	 * @return
	 */
	public BitfinexFundingCurrency getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return "BitfinexFundingSymbol [currency=" + currency + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfinexFundingSymbol other = (BitfinexFundingSymbol) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		return true;
	}
	
}
