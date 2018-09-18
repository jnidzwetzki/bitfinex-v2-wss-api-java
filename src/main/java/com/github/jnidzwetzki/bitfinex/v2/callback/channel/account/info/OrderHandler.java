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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class OrderHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(OrderHandler.class);

    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> eventConsumer = (s, e) -> {};

    public OrderHandler(int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
        logger.info("Got order callback {}", payload.toString());

        // No orders active
        if (payload.isEmpty()) {
            eventConsumer.accept(symbol, Lists.newArrayList());
            return;
        }

        // Snapshot or update
        List<BitfinexSubmittedOrder> orders = Lists.newArrayList();
        if (payload.get(0) instanceof JSONArray) {
            for (int orderPos = 0; orderPos < payload.length(); orderPos++) {
                final JSONArray orderArray = payload.getJSONArray(orderPos);
                BitfinexSubmittedOrder exchangeOrder = jsonToBitfinexSubmittedOrder(orderArray);
                orders.add(exchangeOrder);
            }
        } else {
            BitfinexSubmittedOrder exchangeOrder = jsonToBitfinexSubmittedOrder(payload);
            orders.add(exchangeOrder);
        }
        eventConsumer.accept(symbol, orders);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexSubmittedOrder jsonToBitfinexSubmittedOrder(final JSONArray json) {
        final BitfinexSubmittedOrder order = new BitfinexSubmittedOrder();
        order.setOrderId(json.getLong(0));
        final String gid = json.optString(1, null);
        if (gid != null) {
            order.setClientGroupId(Integer.parseInt(gid));
        }
        final String cid = json.optString(2, null);
        if (cid != null) {
            order.setClientId(Long.parseLong(cid));
        }
        order.setCurrencyPair(BitfinexCurrencyPair.fromSymbolString(json.getString(3)));
        order.setCreatedTimestamp(json.getLong(4));
        final String updatedTimestamp = json.optString(5, null);
        if (updatedTimestamp != null) {
            order.setUpdatedTimestamp(Long.parseLong(updatedTimestamp));
        }
        order.setAmount(json.getBigDecimal(6));
        order.setAmountAtCreation(json.getBigDecimal(7));
        order.setOrderType(BitfinexOrderType.fromBifinexString(json.getString(8)));
        
        final int flags = json.getInt(12);
        if (flags > 0) {      
        		order.setOrderFlags(flags);
        }
        
        final String orderStatus = json.getString(13);
        if (orderStatus != null) {
            order.setStatus(BitfinexSubmittedOrderStatus.fromString(orderStatus));
        }
        
        order.setPrice(json.optBigDecimal(16, null));
        order.setPriceAverage(json.optBigDecimal(17, null));
        order.setPriceTrailing(json.optBigDecimal(18, null));
        order.setPriceAuxLimit(json.optBigDecimal(19, null));
    
        final String parentOrderId = json.optString(25, null);
        if (parentOrderId != null) {
            order.setParentOrderId(Long.parseLong(parentOrderId));
        }
        final String parentOrderType = json.optString(9, null);
        if (parentOrderType != null) {
            order.setParentOrderType(BitfinexOrderType.fromBifinexString(parentOrderType));
        }
        return order;
    }

    /**
     * exchange order event consumer
     *
     * @param consumer of event
     */
    public void onSubmittedOrderEvent(BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> consumer) {
        this.eventConsumer = consumer;
    }

}
