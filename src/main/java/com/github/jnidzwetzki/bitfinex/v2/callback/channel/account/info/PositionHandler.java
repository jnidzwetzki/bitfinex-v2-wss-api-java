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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class PositionHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(PositionHandler.class);

    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>> positionConsumer = (s, p) -> {};

    public PositionHandler(int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
        logger.info("Got position callback {}", payload.toString());

        ArrayList<BitfinexPosition> positions = Lists.newArrayList();
        // No positions active
        if (payload.isEmpty()) {
            positionConsumer.accept(symbol, positions);
            return;
        }

        if (payload.get(0) instanceof JSONArray) {
            // snapshot
            for (int orderPos = 0; orderPos < payload.length(); orderPos++) {
                final JSONArray orderArray = payload.getJSONArray(orderPos);
                BitfinexPosition position = jsonArrayToPosition(orderArray);
                positions.add(position);
            }
        } else {
            // update
            BitfinexPosition position = jsonArrayToPosition(payload);
            positions.add(position);
        }
        positionConsumer.accept(symbol, positions);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexPosition jsonArrayToPosition(final JSONArray json) {
        final String currencyString = json.getString(0);
        final BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString(currencyString);

        final BitfinexPosition position = new BitfinexPosition(currency);
        position.setStatus(json.getString(1));
        position.setAmount(json.getBigDecimal(2));
        position.setBasePrice(json.getBigDecimal(3));
        position.setMarginFunding(json.getBigDecimal(4));
        position.setMarginFundingType(json.optInt(5, -1));
        position.setProfitLoss(json.optBigDecimal(6, null));
        position.setProfitLossPercent(json.optBigDecimal(7, null));
        position.setPriceLiquidation(json.optBigDecimal(8, null));
        position.setLeverage(json.optBigDecimal(9, null));

        return position;
    }

    /**
     * positions event consumer
     * @param consumer of event
     */
    public void onPositionsEvent(BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>> consumer) {
        this.positionConsumer = consumer;
    }
}
