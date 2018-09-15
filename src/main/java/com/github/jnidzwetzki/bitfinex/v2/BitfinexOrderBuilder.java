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

import java.math.BigDecimal;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;

public class BitfinexOrderBuilder {

	private final BitfinexCurrencyPair symbol; 
	private final BitfinexOrderType type;
	private final BigDecimal amount;
	
	private BigDecimal price;
	private BigDecimal priceTrailing;
	private BigDecimal priceAuxLimit;
	private boolean postOnly = false;
	private boolean hidden = false;
	private int groupid = -1;

	private BitfinexOrderBuilder(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final BigDecimal amount) {
		
		this.symbol = symbol;
		this.type = type;
		this.amount = amount;
	}
	
	public static BitfinexOrderBuilder create(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final double amount) {
		
		return create(symbol, type, BigDecimal.valueOf(amount));
	}
	
	public static BitfinexOrderBuilder create(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final BigDecimal amount) {
		
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
		this.price = BigDecimal.valueOf(price);
		return this;
	}
	
	public BitfinexOrderBuilder withPrice(final BigDecimal price) {
		this.price = price;
		return this;
	}
	
	public BitfinexOrderBuilder withPriceTrailing(final double price) {
		this.priceTrailing = BigDecimal.valueOf(price);
		return this;
	}
	
	public BitfinexOrderBuilder withPriceTrailing(final BigDecimal price) {
		this.priceTrailing = price;
		return this;
	}
	
	public BitfinexOrderBuilder withPriceAuxLimit(final double price) {
		this.priceAuxLimit = BigDecimal.valueOf(price);
		return this;
	}
	
	public BitfinexOrderBuilder withPriceAuxLimit(final BigDecimal price) {
		this.priceAuxLimit = price;
		return this;
	}
	
	public BitfinexOrderBuilder withGroupId(final int groupId) {
		this.groupid = groupId;
		return this;
	}

	public BitfinexNewOrder build() {
		BitfinexNewOrder order = new BitfinexNewOrder();
		order.setCurrencyPair(symbol);
		order.setOrderType(type);
		order.setPrice(price);
		order.setAmount(amount);
		order.setPriceTrailing(priceTrailing);
		order.setPriceAuxLimit(priceAuxLimit);
		order.setPostOnly(postOnly);
		order.setHidden(hidden);
		order.setClientGroupId(groupid);
		return order;
	}

}
