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

/**
 * https://docs.bitfinex.com/v2/reference#ws-input-order-new
 */
public class BitfinexNewOrder {

    /**
     * Should be unique in the day (UTC) (not enforced)
     */
    private Long clientId;

    /**
     * (optional) Group id for the order
     */
    private Integer clientGroupId;

    /**
     * currency pair
     */
    private BitfinexCurrencyPair currencyPair;

    /**
     * order amount - pPositive for buy, Negative for sell
     */
    private BigDecimal amount;

    /**
     * order type
     */
    private BitfinexOrderType orderType;

    /**
     * Price (Not required for market orders)
     */
    private BigDecimal price;

    /**
     * The trailing price
     */
    private BigDecimal priceTrailing;

    /**
     * Auxiliary Limit price (for STOP LIMIT)
     */
    private BigDecimal priceAuxLimit;

    /**
     * OCO stop price
     */
    private BigDecimal priceOcoStop;

    /**
     * flags
     * more on them here : https://support.bitfinex.com/hc/en-us/articles/115003506105-Limit-Order
     */
    private boolean postOnly;
    private boolean hidden;
    private boolean close;
    private boolean reduce;
    private boolean oneCancelTheOther;

    private String apiKey;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Integer getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(Integer clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public BitfinexCurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(BitfinexCurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BitfinexOrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(BitfinexOrderType orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceTrailing() {
        return priceTrailing;
    }

    public void setPriceTrailing(BigDecimal priceTrailing) {
        this.priceTrailing = priceTrailing;
    }

    public BigDecimal getPriceAuxLimit() {
        return priceAuxLimit;
    }

    public void setPriceAuxLimit(BigDecimal priceAuxLimit) {
        this.priceAuxLimit = priceAuxLimit;
    }

    public BigDecimal getPriceOcoStop() {
        return priceOcoStop;
    }

    public void setPriceOcoStop(BigDecimal priceOcoStop) {
        this.priceOcoStop = priceOcoStop;
    }

    public boolean isPostOnly() {
        return postOnly;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public boolean isReduce() {
        return reduce;
    }

    public void setReduce(boolean reduce) {
        this.reduce = reduce;
    }

    public boolean isOneCancelTheOther() {
        return oneCancelTheOther;
    }

    public void setOneCancelTheOther(boolean oneCancelTheOther) {
        this.oneCancelTheOther = oneCancelTheOther;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitfinexNewOrder that = (BitfinexNewOrder) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientGroupId, that.clientGroupId) &&
                postOnly == that.postOnly &&
                hidden == that.hidden &&
                close == that.close &&
                reduce == that.reduce &&
                oneCancelTheOther == that.oneCancelTheOther &&
                Objects.equals(currencyPair, that.currencyPair) &&
                Objects.equals(amount, that.amount) &&
                orderType == that.orderType &&
                Objects.equals(price, that.price) &&
                Objects.equals(priceTrailing, that.priceTrailing) &&
                Objects.equals(priceAuxLimit, that.priceAuxLimit) &&
                Objects.equals(priceOcoStop, that.priceOcoStop) &&
                Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientGroupId, currencyPair, amount, orderType, price, priceTrailing, priceAuxLimit, priceOcoStop, postOnly, hidden, close, reduce, oneCancelTheOther, apiKey);
    }

    @Override
    public String toString() {
        return "BitfinexNewOrder [" +
                "clientId=" + clientId +
                ", clientGroupId=" + clientGroupId +
                ", currencyPair=" + currencyPair +
                ", amount=" + amount +
                ", orderType=" + orderType +
                ", price=" + price +
                ", priceTrailing=" + priceTrailing +
                ", priceAuxLimit=" + priceAuxLimit +
                ", priceOcoStop=" + priceOcoStop +
                ", postOnly=" + postOnly +
                ", hidden=" + hidden +
                ", close=" + close +
                ", reduce=" + reduce +
                ", oneCancelTheOther=" + oneCancelTheOther +
                ", apiKey='" + apiKey + '\'' +
                ']';
    }
}
