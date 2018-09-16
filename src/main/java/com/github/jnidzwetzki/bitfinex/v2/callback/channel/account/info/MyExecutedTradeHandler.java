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

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class MyExecutedTradeHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(MyExecutedTradeHandler.class);

    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade> tradeConsumer = (s, t) -> {};

    public MyExecutedTradeHandler(int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
        logger.info("Got trade callback {}", payload.toString());

        BitfinexMyExecutedTrade trade = jsonToTrade(payload);

        // Executed or update
        if ("tu".equals(action)) {
            trade.setUpdate(true);
        }
        tradeConsumer.accept(symbol, trade);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexMyExecutedTrade jsonToTrade(final JSONArray json) {
        final BitfinexMyExecutedTrade trade = new BitfinexMyExecutedTrade();
        trade.setTradeId(json.getLong(0));
        trade.setCurrencyPair(BitfinexCurrencyPair.fromSymbolString(json.getString(1)));
        trade.setTimestamp(json.getLong(2));
        trade.setOrderId(json.getLong(3));
        trade.setAmount(json.getBigDecimal(4));
        trade.setPrice(json.getBigDecimal(5));

        final String orderTypeString = json.optString(6, null);
        if (orderTypeString != null) {
            trade.setOrderType(BitfinexOrderType.fromBifinexString(orderTypeString));
        }

        trade.setOrderPrice(json.optBigDecimal(7, null));
        trade.setMaker(json.getInt(8) == 1);
        trade.setFee(json.optBigDecimal(9, null));
        trade.setFeeCurrency(json.optString(10, null));
        return trade;
    }

    public void onTradeEvent(BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade> tradeConsumer) {
        this.tradeConsumer = tradeConsumer;
    }
}
