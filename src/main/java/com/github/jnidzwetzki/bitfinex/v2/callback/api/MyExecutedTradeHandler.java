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

import java.util.function.Consumer;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;

public class MyExecutedTradeHandler implements APICallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(MyExecutedTradeHandler.class);

    private Consumer<BitfinexMyExecutedTrade> tradeConsumer = t -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final JSONArray jsonArray) throws APIException {
        logger.info("Got trade callback {}", jsonArray.toString());

        final String type = jsonArray.getString(1);
        final JSONArray message = jsonArray.getJSONArray(2);

        BitfinexMyExecutedTrade trade = jsonToTrade(message);

        // Executed or update
        if ("tu".equals(type)) {
            trade.setUpdate(true);
        }
        tradeConsumer.accept(trade);
    }

    private BitfinexMyExecutedTrade jsonToTrade(final JSONArray json) {
        final BitfinexMyExecutedTrade trade = new BitfinexMyExecutedTrade();
        trade.setTradeId(json.getLong(0));
        trade.setCurrency(BitfinexCurrencyPair.fromSymbolString(json.getString(1)));
        trade.setTimestamp(json.getLong(2));
        trade.setOrderId(json.getLong(3));
        trade.setAmount(json.getBigDecimal(4));
        trade.setPrice(json.getBigDecimal(5));

        final String orderTypeString = json.optString(6, null);
        if (orderTypeString != null) {
            trade.setOrderType(BitfinexOrderType.fromString(orderTypeString));
        }

        trade.setOrderPrice(json.optBigDecimal(7, null));
        trade.setMaker(json.getInt(8) == 1);
        trade.setFee(json.optBigDecimal(9, null));
        trade.setFeeCurrency(json.optString(10, null));
        return trade;
    }

    public void onTradeEvent(Consumer<BitfinexMyExecutedTrade> tradeConsumer) {
        this.tradeConsumer = tradeConsumer;
    }
}
