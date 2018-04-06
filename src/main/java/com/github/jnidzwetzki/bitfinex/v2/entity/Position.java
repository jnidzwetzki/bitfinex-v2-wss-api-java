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
	private BigDecimal marginFundingType;
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
	
	@Deprecated
	public double getAmount() {
		return amount.doubleValue();
	}
	
	public BigDecimal getAmountAsBigDecimal() {
		return amount;
	}
	
	public void setAmount(final double amount) {
		this.amount = BigDecimal.valueOf(amount);
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	@Deprecated
	public double getBasePrice() {
		return basePrice.doubleValue();
	}
	
	public BigDecimal getBasePriceAsBigDecimal() {
		return basePrice;
	}
	
	public void setBasePrice(final double basePrice) {
		this.basePrice = BigDecimal.valueOf(basePrice);
	}
	
	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}
	
	@Deprecated
	public double getMarginFunding() {
		return marginFunding.doubleValue();
	}
	
	public BigDecimal getMarginFundingAsBigDecimal() {
		return marginFunding;
	}
	
	public void setMarginFunding(final double marginFunding) {
		this.marginFunding = BigDecimal.valueOf(marginFunding);
	}
	
	public void setMarginFunding(BigDecimal marginFunding) {
		this.marginFunding = marginFunding;
	}
	
	@Deprecated
	public double getMarginFundingType() {
		return marginFundingType.doubleValue();
	}
	
	public BigDecimal getMarginFundingTypeAsBigDecimal() {
		return marginFundingType;
	}
	
	public void setMarginFundingType(final double marginFundingType) {
		this.marginFundingType = BigDecimal.valueOf(marginFundingType);
	}
	
	public void setMarginFundingType(BigDecimal marginFundingType) {
		this.marginFundingType = marginFundingType;
	}
	
	@Deprecated
	public double getPl() {
		return pl.doubleValue();
	}
	
	public BigDecimal getPlAsBigDecimal() {
		return pl;
	}
	
	
	public void setPl(final double pl) {
		this.pl = BigDecimal.valueOf(pl);
	}
	
	public void setPl(BigDecimal pl) {
		this.pl = pl;
	}
	
	@Deprecated
	public double getPriceLiquidation() {
		return priceLiquidation.doubleValue();
	}
	
	public BigDecimal getPriceLiquidationAsBigDecimal() {
		return priceLiquidation;
	}
	
	public void setPriceLiquidation(final double priceLiquidation) {
		this.priceLiquidation = BigDecimal.valueOf(priceLiquidation);
	}
	
	public void setPriceLiquidation(BigDecimal priceLiquidation) {
		this.priceLiquidation = priceLiquidation;
	}
	
	@Deprecated
	public double getLeverage() {
		return leverage.doubleValue();
	}
	
	public BigDecimal getLeverageAsBigDecimal() {
		return leverage;
	}
	
	public void setLeverage(final double leverage) {
		this.leverage = BigDecimal.valueOf(leverage);
	}
	
	public void setLeverage(BigDecimal leverage) {
		this.leverage = leverage;
	}

	@Deprecated
	public double getPlPercent() {
		return plPercent.doubleValue();
	}
	
	public BigDecimal getPlPercentAsBigDecimal() {
		return plPercent;
	}

	public void setPlPercent(final double plPercent) {
		this.plPercent = BigDecimal.valueOf(plPercent);
	}
	
	public void setPlPercent(BigDecimal plPercent) {
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
