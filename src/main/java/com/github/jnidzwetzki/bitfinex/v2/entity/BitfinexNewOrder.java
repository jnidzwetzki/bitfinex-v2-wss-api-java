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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
     * The Order flags
     */
    private Set<BitfinexOrderFlag> orderFlags = new HashSet<>();

    /**
     * The api key
     */
    private String apiKey;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(final Long clientId) {
        this.clientId = clientId;
    }

    public Integer getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(final Integer clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public BitfinexCurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(final BitfinexCurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BitfinexOrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(final BitfinexOrderType orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceTrailing() {
        return priceTrailing;
    }

    public void setPriceTrailing(final BigDecimal priceTrailing) {
        this.priceTrailing = priceTrailing;
    }

    public BigDecimal getPriceAuxLimit() {
        return priceAuxLimit;
    }

    public void setPriceAuxLimit(final BigDecimal priceAuxLimit) {
        this.priceAuxLimit = priceAuxLimit;
    }

    public BigDecimal getPriceOcoStop() {
        return priceOcoStop;
    }

    public void setPriceOcoStop(final BigDecimal priceOcoStop) {
        this.priceOcoStop = priceOcoStop;
    }
 
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }
    
	public void setOrderFlags(final Set<BitfinexOrderFlag> orderFlags) {
		this.orderFlags = orderFlags;
	}
	
	public Set<BitfinexOrderFlag> getOrderFlags() {
		return orderFlags;
	}
	
	/**
	 * Convert a flag field into enums
	 * @param flags
	 */
	public void setOrderFlags(final int flags) {
		orderFlags = Arrays.
				stream(BitfinexOrderFlag.values())
				.filter(f -> ((f.getFlag() & flags) == f.getFlag()))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Convert flag enums to flag field
	 * @return
	 */
	public int getCombinedFlags() {
		return orderFlags
			.stream()
			.map(o -> o.getFlag())
			.reduce((f1, f2) -> f1 | f2)
			.orElse(0);
	}

	@Override
	public String toString() {
		return "BitfinexNewOrder [clientId=" + clientId + ", clientGroupId=" + clientGroupId + ", currencyPair="
				+ currencyPair + ", amount=" + amount + ", orderType=" + orderType + ", price=" + price
				+ ", priceTrailing=" + priceTrailing + ", priceAuxLimit=" + priceAuxLimit + ", priceOcoStop="
				+ priceOcoStop + ", orderFlags=" + orderFlags + ", apiKey=" + apiKey + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
		result = prime * result + ((clientGroupId == null) ? 0 : clientGroupId.hashCode());
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((currencyPair == null) ? 0 : currencyPair.hashCode());
		result = prime * result + ((orderFlags == null) ? 0 : orderFlags.hashCode());
		result = prime * result + ((orderType == null) ? 0 : orderType.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + ((priceAuxLimit == null) ? 0 : priceAuxLimit.hashCode());
		result = prime * result + ((priceOcoStop == null) ? 0 : priceOcoStop.hashCode());
		result = prime * result + ((priceTrailing == null) ? 0 : priceTrailing.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfinexNewOrder other = (BitfinexNewOrder) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (apiKey == null) {
			if (other.apiKey != null)
				return false;
		} else if (!apiKey.equals(other.apiKey))
			return false;
		if (clientGroupId == null) {
			if (other.clientGroupId != null)
				return false;
		} else if (!clientGroupId.equals(other.clientGroupId))
			return false;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (currencyPair == null) {
			if (other.currencyPair != null)
				return false;
		} else if (!currencyPair.equals(other.currencyPair))
			return false;
		if (orderFlags == null) {
			if (other.orderFlags != null)
				return false;
		} else if (!orderFlags.equals(other.orderFlags))
			return false;
		if (orderType != other.orderType)
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (priceAuxLimit == null) {
			if (other.priceAuxLimit != null)
				return false;
		} else if (!priceAuxLimit.equals(other.priceAuxLimit))
			return false;
		if (priceOcoStop == null) {
			if (other.priceOcoStop != null)
				return false;
		} else if (!priceOcoStop.equals(other.priceOcoStop))
			return false;
		if (priceTrailing == null) {
			if (other.priceTrailing != null)
				return false;
		} else if (!priceTrailing.equals(other.priceTrailing))
			return false;
		return true;
	}

}
