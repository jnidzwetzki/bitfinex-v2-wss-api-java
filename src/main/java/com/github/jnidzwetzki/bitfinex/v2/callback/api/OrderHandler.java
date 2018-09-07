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
package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;

public class OrderHandler implements APICallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(OrderHandler.class);
    private Consumer<Collection<BitfinexSubmittedOrder>> exchangeOrdersConsumer = eos -> {
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final JSONArray message) throws APIException {
        logger.info("Got order callback {}", message.toString());

        final JSONArray json = message.getJSONArray(2);

        // No orders active
        if (json.length() == 0) {
            exchangeOrdersConsumer.accept(Lists.newArrayList());
            return;
        }

        // Snapshot or update
        List<BitfinexSubmittedOrder> orders = Lists.newArrayList();
        if (json.get(0) instanceof JSONArray) {
            for (int orderPos = 0; orderPos < json.length(); orderPos++) {
                final JSONArray orderArray = json.getJSONArray(orderPos);
                BitfinexSubmittedOrder exchangeOrder = jsonToBitfinexSubmittedOrder(orderArray);
                orders.add(exchangeOrder);
            }
        } else {
            BitfinexSubmittedOrder exchangeOrder = jsonToBitfinexSubmittedOrder(json);
            orders.add(exchangeOrder);
        }
        exchangeOrdersConsumer.accept(orders);
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
        order.setSymbol(BitfinexCurrencyPair.fromSymbolString(json.getString(3)));
        order.setCreatedTimestamp(json.getLong(4));
        final String updatedTimestamp = json.optString(5, null);
        if (updatedTimestamp != null) {
            order.setUpdatedTimestamp(Long.parseLong(updatedTimestamp));
        }
        order.setAmount(json.getBigDecimal(6));
        order.setAmountAtCreation(json.getBigDecimal(7));
        order.setOrderType(BitfinexOrderType.fromBifinexString(json.getString(8)));
        // FIXME: investigate here - documentation is not specifying any numbers
        // FIXME: https://docs.bitfinex.com/v2/reference#ws-auth-orders
        final int flags = json.getInt(12);
        if (flags > 0) {
            logger.info("Flags set on order: " + flags);
        }
        final String orderStatus = json.getString(13);
        if (orderStatus != null) {
            order.setStatus(BitfinexSubmittedOrderStatus.fromString(orderStatus));
        }
        order.setPrice(json.optBigDecimal(16, null));
        order.setPriceAverage(json.optBigDecimal(17, null));
        order.setPriceTrailing(json.optBigDecimal(18, null));
        order.setPriceAuxLimit(json.optBigDecimal(19, null));
        order.setNotify(json.getInt(23) == 1);
        order.setHidden(json.getInt(24) == 1); // TODO: remove it, hidden is passed through flags
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
    public void onSubmittedOrderEvent(Consumer<Collection<BitfinexSubmittedOrder>> consumer) {
        this.exchangeOrdersConsumer = consumer;
    }

}
