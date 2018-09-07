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

import java.math.BigDecimal;

public class BitfinexOrderBookEntry {

	private final Long orderId;
	private final BigDecimal price;
	private final BigDecimal amount;
	private final Integer count;

	public BitfinexOrderBookEntry(Long orderId, BigDecimal price, BigDecimal amount, Integer count) {
		this.orderId = orderId;
		this.price = price;
		this.amount = amount;
		this.count = count;
	}

	public Long getOrderId() {
		return orderId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "BitfinexOrderBookEntry [price=" + price + ", count=" + count + ", amount=" + amount + "]";
	}

}
