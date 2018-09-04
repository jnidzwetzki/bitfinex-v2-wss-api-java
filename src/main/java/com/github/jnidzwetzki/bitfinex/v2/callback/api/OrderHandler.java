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

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class OrderHandler implements APICallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(OrderHandler.class);
    private Consumer<Collection<ExchangeOrder>> exchangeOrdersConsumer = eos -> {};

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
        List<ExchangeOrder> orders = Lists.newArrayList();
        if (json.get(0) instanceof JSONArray) {
            for (int orderPos = 0; orderPos < json.length(); orderPos++) {
                final JSONArray orderArray = json.getJSONArray(orderPos);
                ExchangeOrder exchangeOrder = jsonToExchangeOrder(orderArray);
                orders.add(exchangeOrder);
            }
        } else {
            ExchangeOrder exchangeOrder = jsonToExchangeOrder(json);
            orders.add(exchangeOrder);
        }
        exchangeOrdersConsumer.accept(orders);
    }

    private ExchangeOrder jsonToExchangeOrder(final JSONArray json) {
        final ExchangeOrder exchangeOrder = new ExchangeOrder();
        exchangeOrder.setOrderId(json.getLong(0));
        exchangeOrder.setGroupId(json.optInt(1, -1));
        exchangeOrder.setCid(json.optLong(2, -1));
        exchangeOrder.setSymbol(json.getString(3));
        exchangeOrder.setCreated(json.getLong(4));
        exchangeOrder.setUpdated(json.getLong(5));
        exchangeOrder.setAmount(json.getBigDecimal(6));
        exchangeOrder.setAmountAtCreation(json.getBigDecimal(7));
        exchangeOrder.setOrderType(BitfinexOrderType.fromString(json.getString(8)));

        final ExchangeOrderState orderState = ExchangeOrderState.fromString(json.getString(13));
        exchangeOrder.setState(orderState);

        exchangeOrder.setPrice(json.optBigDecimal(16, null));
        exchangeOrder.setPriceAvg(json.optBigDecimal(17, null));
        exchangeOrder.setPriceTrailing(json.optBigDecimal(18, null));
        exchangeOrder.setPriceAuxLimit(json.optBigDecimal(19, null));
        exchangeOrder.setNotify(json.getInt(23) == 1);
        exchangeOrder.setHidden(json.getInt(24) == 1);

        return exchangeOrder;

    }

    /**
     * exchange order event consumer
     * @param consumer of event
     */
    public void onExchangeOrdersEvent(Consumer<Collection<ExchangeOrder>> consumer) {
        this.exchangeOrdersConsumer = consumer;
    }
}
