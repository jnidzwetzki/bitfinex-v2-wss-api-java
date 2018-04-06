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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bboxdb.commons.MicroSecondTimestampProvider;

@Entity
@Table(name = "orders_executed")
public class BitfinexOrder {

	@Id
	@GeneratedValue
	private long id;
	
	private final long cid;
	
	private String apikey;
	
	@Enumerated(EnumType.STRING)
	private final BitfinexCurrencyPair symbol; 
	
	@Enumerated(EnumType.STRING)
	private final BitfinexOrderType type;
	
	private BigDecimal price;
	private BigDecimal priceTrailing;
	private BigDecimal priceAuxLimit;
	private BigDecimal amount;
	private final boolean postOnly;
	private final boolean hidden;
	private final int groupId;
	
	/**
	 * Needed for hibernate
	 */
	public BitfinexOrder() {
		// The client ID
		this.cid = MicroSecondTimestampProvider.getNewTimestamp();

		this.symbol = null;
		this.apikey = null;
		this.type = null;
		this.price = null;
		this.priceTrailing = null;
		this.priceAuxLimit = null;
		this.amount = null;
		this.postOnly = false;
		this.hidden = false;
		this.groupId = -1;
	}
	
	public BitfinexOrder(final BitfinexCurrencyPair symbol, final BitfinexOrderType type, 
			final BigDecimal price, final BigDecimal amount, final BigDecimal priceTrailing, 
			final BigDecimal priceAuxLimit, final boolean postOnly, final boolean hidden,
			final int groupId) {
		
		// The client ID
		this.cid = MicroSecondTimestampProvider.getNewTimestamp();

		this.symbol = symbol;
		this.type = type;
		this.price = price;
		this.priceTrailing = priceTrailing;
		this.priceAuxLimit = priceAuxLimit;
		this.amount = amount;
		this.postOnly = postOnly;
		this.hidden = hidden;
		this.groupId = groupId;
	}

	@Override
	public String toString() {
		return "BitfinexOrder [id=" + id + ", cid=" + cid + ", apikey=" + apikey + ", symbol=" + symbol + ", type="
				+ type + ", price=" + price + ", priceTrailing=" + priceTrailing + ", priceAuxLimit=" + priceAuxLimit
				+ ", amount=" + amount + ", postOnly=" + postOnly + ", hidden=" + hidden + ", groupId=" + groupId + "]";
	}

	public BitfinexCurrencyPair getSymbol() {
		return symbol;
	}

	public BitfinexOrderType getType() {
		return type;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public BigDecimal getPriceTrailing() {
		return priceTrailing;
	}

	public BigDecimal getPriceAuxLimit() {
		return priceAuxLimit;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public boolean isPostOnly() {
		return postOnly;
	}

	public boolean isHidden() {
		return hidden;
	}

	public long getCid() {
		return cid;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(final String apikey) {
		this.apikey = apikey;
	}
	
}
