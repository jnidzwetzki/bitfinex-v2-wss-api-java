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
package com.github.jnidzwetzki.bitfinex.v2;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;

public class BitfinexOrderBuilder {

	private final BitfinexCurrencyPair symbol; 
	private final BitfinexOrderType type;
	private final double amount;
	
	private double price = -1;
	private double priceTrailing = -1;
	private double priceAuxLimit = -1;
	private boolean postOnly = false;
	private boolean hidden = false;
	private int groupid = -1;

	private BitfinexOrderBuilder(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final double amount) {
		
		this.symbol = symbol;
		this.type = type;
		this.amount = amount;
	}
	
	public static BitfinexOrderBuilder create(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final double amount) {
		
		return new BitfinexOrderBuilder(symbol, type, amount);
	}
	
	public BitfinexOrderBuilder setHidden() {
		hidden = true;
		return this;
	}
	
	public BitfinexOrderBuilder setPostOnly() {
		postOnly = true;
		return this;
	}
	
	public BitfinexOrderBuilder withPrice(final double price) {
		this.price = price;
		return this;
	}
	
	public BitfinexOrderBuilder withPriceTrailing(final double price) {
		this.priceTrailing = price;
		return this;
	}
	
	public BitfinexOrderBuilder withPriceAuxLimit(final double price) {
		this.priceAuxLimit = price;
		return this;
	}
	
	public BitfinexOrderBuilder withGroupId(final int groupId) {
		this.groupid = groupId;
		return this;
	}
	
	public BitfinexOrder build() {
		return new BitfinexOrder(symbol, type, price, 
				amount, priceTrailing, priceAuxLimit, postOnly, hidden, groupid);
	}

}
