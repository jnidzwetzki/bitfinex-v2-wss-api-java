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

public class BitfinexMyExecutedTrade extends BitfinexExecutedTrade {

	/**
	 * order which triggered this trade
	 */
	private Long orderId;

	/**
	 * currency symbol of trade
	 */
	private BitfinexCurrencyPair currency;

	/**
	 * order type that triggered this trade
	 */
	private BitfinexOrderType orderType;

	/**
	 * orderbook price of the order that triggered this trade
	 */
	private BigDecimal orderPrice;

	/**
	 * is maker or taker
	 */
	private boolean maker;

	/**
	 * received only in 'TU' event
	 * charged fee for this trade
	 */
	private BigDecimal fee;

	/**
	 * received only in 'TU' event
	 * currency used by fee
	 */
	private String feeCurrency;

	/**
	 * true if this trade event is updating one
	 */
	private boolean update;

	/**
	 * api key which received this info
	 */
	private String apiKey;

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public BitfinexCurrencyPair getCurrency() {
		return currency;
	}

	public void setCurrency(BitfinexCurrencyPair currency) {
		this.currency = currency;
	}

	public BitfinexOrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(BitfinexOrderType orderType) {
		this.orderType = orderType;
	}

	public BigDecimal getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(BigDecimal orderPrice) {
		this.orderPrice = orderPrice;
	}

	public boolean isMaker() {
		return maker;
	}

	public void setMaker(boolean maker) {
		this.maker = maker;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public String getFeeCurrency() {
		return feeCurrency;
	}

	public void setFeeCurrency(String feeCurrency) {
		this.feeCurrency = feeCurrency;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		BitfinexMyExecutedTrade that = (BitfinexMyExecutedTrade) o;
		return Objects.equals(update, that.update) &&
				Objects.equals(apiKey, that.apiKey) &&
				Objects.equals(orderId, that.orderId) &&
				Objects.equals(currency, that.currency) &&
				orderType == that.orderType &&
				Objects.equals(orderPrice, that.orderPrice) &&
				Objects.equals(maker, that.maker) &&
				Objects.equals(fee, that.fee) &&
				Objects.equals(feeCurrency, that.feeCurrency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), update, apiKey, orderId, currency, orderType, orderPrice, maker, fee, feeCurrency);
	}

	@Override
	public String toString() {
		return "BitfinexMyExecutedTrade [" +
				"id=" + getTradeId() +
				", executed=" + update +
				", apiKey='" + apiKey + '\'' +
				", orderId=" + orderId +
				", currency=" + currency +
				", orderType=" + orderType +
				", orderPrice=" + orderPrice +
				", maker=" + maker +
				", fee=" + fee +
				", feeCurrency='" + feeCurrency + '\'' +
				", timestamp=" + getTimestamp() +
				", amount=" + getAmount() +
				", price=" + getPrice() +
				", rate=" + getRate() +
				", period=" + getPeriod() +
				']';
	}
}
