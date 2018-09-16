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

import java.util.function.BiConsumer;

import com.google.common.base.Strings;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class NotificationHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(NotificationHandler.class);
    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> submittedOrderConsumer = (s, e) -> {};

    public NotificationHandler(int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
        logger.debug("Got notification callback {}", payload.toString());

        if (payload.isEmpty()) {
            return;
        }

        // Test for order error callback
        // [0,"n",[null,"on-req",null,null,[null,null,1513970684865000,"tBTCUSD",null,null,0.001,0.001,"EXCHANGE MARKET",null,null,null,null,null,null,null,12940,null,null,null,null,null,null,0,null,null],null,"ERROR","Invalid order: minimum size for BTC/USD is 0.002"]]
        if ("on-req".equals(payload.getString(1))) {
            final String state = payload.optString(6);
            if ("ERROR".equals(state)) {
                BitfinexSubmittedOrder exchangeOrder = jsonToBitfinexSubmittedOrder(payload);
                submittedOrderConsumer.accept(symbol, exchangeOrder);
            }
        }
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexSubmittedOrder jsonToBitfinexSubmittedOrder(JSONArray array) {
        final JSONArray orderJson = array.optJSONArray(4);
        final long oid = orderJson.optLong(0, -1);
        final int gid = orderJson.optInt(1, -1);
        final long cid = orderJson.optLong(2, -1);
        final String symbol = orderJson.optString(3);
        final String stateValue = array.optString(7);
        final String state = array.optString(6);

        final BitfinexSubmittedOrder submittedOrder = new BitfinexSubmittedOrder();
        if (oid != -1) {
            submittedOrder.setOrderId(oid);
        }
        if (gid != -1) {
            submittedOrder.setClientGroupId(gid);
        }
        if( cid != -1) {
            submittedOrder.setClientId(cid);
        }

        if (!Strings.isNullOrEmpty(symbol)) {
            submittedOrder.setCurrencyPair(BitfinexCurrencyPair.fromSymbolString(symbol));
        }
        submittedOrder.setStatus(BitfinexSubmittedOrderStatus.ERROR);
        logger.error("State for order {} is {}, reason is {}", submittedOrder.getOrderId(), state, stateValue);
        return submittedOrder;
    }

    /**
     * exchange order notification consumer
     * @param consumer of event
     */
    public void onOrderNotification(BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> consumer) {
        this.submittedOrderConsumer = consumer;
    }
}
