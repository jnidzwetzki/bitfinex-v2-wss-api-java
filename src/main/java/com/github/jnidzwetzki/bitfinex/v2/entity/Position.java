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

public class Position {

	private final BitfinexCurrencyPair curreny;
	private String status;
	private BigDecimal amount;
	private BigDecimal basePrice;
	private BigDecimal marginFunding;
	private int marginFundingType;
	private BigDecimal pl;
	private BigDecimal plPercent;
	private BigDecimal priceLiquidation;
	private BigDecimal leverage;
	
	public Position(final BitfinexCurrencyPair curreny) {
		this.curreny = curreny;
	}

	public BitfinexCurrencyPair getCurreny() {
		return curreny;
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
	
	public BigDecimal getPl() {
		return pl;
	}
	
	public void setPl(final BigDecimal pl) {
		this.pl = pl;
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

	public BigDecimal getPlPercent() {
		return plPercent;
	}
	
	public void setPlPercent(final BigDecimal plPercent) {
		this.plPercent = plPercent;
	}

	@Override
	public String toString() {
		return "Position [curreny=" + curreny + ", status=" + status + ", amount=" + amount + ", basePrice=" + basePrice
				+ ", marginFunding=" + marginFunding + ", marginFundingType=" + marginFundingType + ", pl=" + pl
				+ ", plPercent=" + plPercent + ", priceLiquidation=" + priceLiquidation + ", leverage=" + leverage
				+ "]";
	}
	
}
