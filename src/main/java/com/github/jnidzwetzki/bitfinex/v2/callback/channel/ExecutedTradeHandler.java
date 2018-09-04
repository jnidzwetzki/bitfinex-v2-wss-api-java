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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import org.json.JSONArray;
import org.json.JSONException;

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class ExecutedTradeHandler implements ChannelCallbackHandler {

    private BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>> executedTradesConsumer = (s, t) -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {
        try {
            final BitfinexExecutedTradeSymbol config = (BitfinexExecutedTradeSymbol) channelSymbol;
            final List<ExecutedTrade> trades = new ArrayList<>();

            // Snapshots contain multiple executes entries, updates only one
            if (jsonArray.get(0) instanceof JSONArray) {
                for (int pos = 0; pos < jsonArray.length(); pos++) {
                    final JSONArray parts = jsonArray.getJSONArray(pos);
                    ExecutedTrade trade = jsonToExecutedTrade(parts);
                    trades.add(trade);
                }
            } else {
                ExecutedTrade trade = jsonToExecutedTrade(jsonArray);
                trades.add(trade);
            }
            executedTradesConsumer.accept(config, trades);
        } catch (JSONException e) {
            throw new APIException(e);
        }
    }

    private ExecutedTrade jsonToExecutedTrade(final JSONArray jsonArray) {
        final ExecutedTrade executedTrade = new ExecutedTrade();

        final long id = jsonArray.getNumber(0).longValue();
        executedTrade.setId(id);

        final long timestamp = jsonArray.getNumber(1).longValue();
        executedTrade.setTimestamp(timestamp);

        final BigDecimal amount = jsonArray.getBigDecimal(2);
        executedTrade.setAmount(amount);

        // Funding or Currency
        if (jsonArray.optNumber(4) != null) {
            final BigDecimal rate = jsonArray.getBigDecimal(3);
            executedTrade.setRate(rate);

            final int period = jsonArray.getNumber(4).intValue();
            executedTrade.setPeriod(period);
        } else {
            final BigDecimal price = jsonArray.getBigDecimal(3);
            executedTrade.setPrice(price);
        }
        return executedTrade;
    }

    /**
     * candlestick consumer
     *
     * @param consumer of event
     */
    public void onExecutedTradeEvent(BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>> consumer) {
        this.executedTradesConsumer = consumer;
    }
}
