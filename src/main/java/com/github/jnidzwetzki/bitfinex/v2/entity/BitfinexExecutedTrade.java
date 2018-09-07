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
import java.util.Objects;

public class BitfinexExecutedTrade {

	/**
	 * trade id
	 */
	private Long tradeId;

	/**
	 * execution timestamp
	 */
	private Long timestamp;

	/**
	 * amount - positive for buy, negative for sell
	 */
	private BigDecimal amount;

	/**
	 * price at which trade was executed
	 */
	private BigDecimal price;

	/**
	 * !! USED ONLY BY FUNDING
	 * Rate at which funding transaction occurred
	 */
	private BigDecimal rate;

	/**
	 * !! USED ONLY BY FUNDING
	 * Amount of time the funding transaction was for
	 */
	private Long period;


	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BitfinexExecutedTrade that = (BitfinexExecutedTrade) o;
		return Objects.equals(tradeId, that.tradeId) &&
				Objects.equals(timestamp, that.timestamp) &&
				Objects.equals(amount, that.amount) &&
				Objects.equals(price, that.price) &&
				Objects.equals(rate, that.rate) &&
				Objects.equals(period, that.period);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tradeId, timestamp, amount, price, rate, period);
	}

	@Override
	public String toString() {
		return "BitfinexExecutedTrade [" +
				"tradeId=" + tradeId +
				", timestamp=" + timestamp +
				", amount=" + amount +
				", price=" + price +
				", rate=" + rate +
				", period=" + period +
				']';
	}
}
