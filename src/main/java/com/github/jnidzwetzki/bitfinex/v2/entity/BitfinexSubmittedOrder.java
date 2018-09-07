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
 * https://docs.bitfinex.com/v2/reference#ws-auth-orders
 */
public class BitfinexSubmittedOrder extends BitfinexNewOrder {

    /**
     * exchange assigned order id
     */
    private Long orderId;

    /**
     * Millisecond timestamp of creation
     */
    private Long createdTimestamp;

    /**
     * Millisecond timestamp of last update
     */
    private Long updatedTimestamp;

    /**
     * order amount - positive means buy, negative means sell.
     */
    private BigDecimal amount;

    /**
     * original amount - at creation
     */
    private BigDecimal amountAtCreation;


    /**
     * Order Status: ACTIVE, EXECUTED @ PRICE(AMOUNT) e.g. "EXECUTED @ 107.6(-0.2)", PARTIALLY FILLED @ PRICE(AMOUNT), INSUFFICIENT MARGIN was: PARTIALLY FILLED @ PRICE(AMOUNT), CANCELED, CANCELED was: PARTIALLY FILLED @ PRICE(AMOUNT)
     */
    private BitfinexSubmittedOrderStatus status;

    /**
     * Average price
     */
    private BigDecimal priceAverage;

    /**
     * If another order caused this order to be placed (OCO) this will be that other order's ID
     */
    private Long parentOrderId;

    /**
     * Order (parent) type that triggered this order
     */
    private BitfinexOrderType parentOrderType;

    /**
     * no var rates flag
     */
    private boolean noVarRates;

    /**
     * notify flag
     */
    private boolean notify;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountAtCreation() {
        return amountAtCreation;
    }

    public void setAmountAtCreation(BigDecimal amountAtCreation) {
        this.amountAtCreation = amountAtCreation;
    }

    public BitfinexSubmittedOrderStatus getStatus() {
        return status;
    }

    public void setStatus(BitfinexSubmittedOrderStatus status) {
        this.status = status;
    }

    public BigDecimal getPriceAverage() {
        return priceAverage;
    }

    public void setPriceAverage(BigDecimal priceAverage) {
        this.priceAverage = priceAverage;
    }

    public Long getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(Long parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public BitfinexOrderType getParentOrderType() {
        return parentOrderType;
    }

    public void setParentOrderType(BitfinexOrderType parentOrderType) {
        this.parentOrderType = parentOrderType;
    }

    public boolean isNoVarRates() {
        return noVarRates;
    }

    public void setNoVarRates(boolean noVarRates) {
        this.noVarRates = noVarRates;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BitfinexSubmittedOrder that = (BitfinexSubmittedOrder) o;
        return noVarRates == that.noVarRates &&
                notify == that.notify &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(createdTimestamp, that.createdTimestamp) &&
                Objects.equals(updatedTimestamp, that.updatedTimestamp) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(amountAtCreation, that.amountAtCreation) &&
                status == that.status &&
                Objects.equals(priceAverage, that.priceAverage) &&
                Objects.equals(parentOrderId, that.parentOrderId) &&
                parentOrderType == that.parentOrderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderId, createdTimestamp, updatedTimestamp, amount, amountAtCreation, status, priceAverage, parentOrderId, parentOrderType, noVarRates, notify);
    }

    @Override
    public String toString() {
        return "BitfinexSubmittedOrder [" +
                "orderId=" + orderId +
                ", createdTimestamp=" + createdTimestamp +
                ", updatedTimestamp=" + updatedTimestamp +
                ", amount=" + amount +
                ", amountAtCreation=" + amountAtCreation +
                ", status=" + status +
                ", priceAverage=" + priceAverage +
                ", parentOrderId=" + parentOrderId +
                ", parentOrderType=" + parentOrderType +
                ", noVarRates=" + noVarRates +
                ", notify=" + notify +
                ']';
    }
}
