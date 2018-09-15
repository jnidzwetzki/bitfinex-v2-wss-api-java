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

public class BitfinexPosition {

	private final BitfinexCurrencyPair currencyPair;
	private String status;
	private BigDecimal amount;
	private BigDecimal basePrice;
	private BigDecimal marginFunding;
	private int marginFundingType;
	private BigDecimal profitLoss;
	private BigDecimal profitLossPercent;
	private BigDecimal priceLiquidation;
	private BigDecimal leverage;
	
	public BitfinexPosition(final BitfinexCurrencyPair currencyPair) {
		this.currencyPair = currencyPair;
	}

	public BitfinexCurrencyPair getCurrencyPair() {
		return currencyPair;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(final String status) {
		this.status = status;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}
	
	public BigDecimal getBasePrice() {
		return basePrice;
	}
	
	public void setBasePrice(final BigDecimal basePrice) {
		this.basePrice = basePrice;
	}
	
	public BigDecimal getMarginFunding() {
		return marginFunding;
	}
	
	public void setMarginFunding(final BigDecimal marginFunding) {
		this.marginFunding = marginFunding;
	}
	
	public int getMarginFundingType() {
		return marginFundingType;
	}
	
	public void setMarginFundingType(final int marginFundingType) {
		this.marginFundingType = marginFundingType;
	}
	
	public BigDecimal getProfitLoss() {
		return profitLoss;
	}
	
	public void setProfitLoss(final BigDecimal pl) {
		this.profitLoss = pl;
	}
	
	public BigDecimal getPriceLiquidation() {
		return priceLiquidation;
	}
	
	public void setPriceLiquidation(final BigDecimal priceLiquidation) {
		this.priceLiquidation = priceLiquidation;
	}
	
	public BigDecimal getLeverage() {
		return leverage;
	}
	
	public void setLeverage(final BigDecimal leverage) {
		this.leverage = leverage;
	}

	public BigDecimal getProfitLossPercent() {
		return profitLossPercent;
	}
	
	public void setProfitLossPercent(final BigDecimal profitLossPercent) {
		this.profitLossPercent = profitLossPercent;
	}

	@Override
	public String toString() {
		return "BitfinexPosition [currencyPair=" + currencyPair + ", status=" + status + ", amount=" + amount + ", basePrice=" + basePrice
				+ ", marginFunding=" + marginFunding + ", marginFundingType=" + marginFundingType + ", profitLoss=" + profitLoss
				+ ", profitLossPercent=" + profitLossPercent + ", priceLiquidation=" + priceLiquidation + ", leverage=" + leverage
				+ "]";
	}
	
}
