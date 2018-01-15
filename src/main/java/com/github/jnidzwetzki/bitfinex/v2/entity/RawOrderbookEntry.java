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

public class RawOrderbookEntry {

	private final double orderId;
	private final double price;
	private final double amount;

	public RawOrderbookEntry(final double orderId, final double price, final double amount) {
		this.orderId = orderId;
		this.price = price;
		this.amount = amount;
	}

	public double getOrderId() {
		return orderId;
	}

	public double getPrice() {
		return price;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "RawOrderbookEntry [orderId=" + orderId + ", price=" + price + ", amount=" + amount + "]";
	}

}
