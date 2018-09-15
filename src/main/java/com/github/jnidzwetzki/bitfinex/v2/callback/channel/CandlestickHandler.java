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
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class CandlestickHandler implements ChannelCallbackHandler {

    private final int channelId;
    private final BitfinexCandlestickSymbol symbol;

    private BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> candlesConsumer = (c, l) -> {};

    public CandlestickHandler(int channelId, final BitfinexCandlestickSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
        if (payload.isEmpty()) {
            return;
        }
        // channel symbol trade:1m:tLTCUSD
        final Set<BitfinexCandle> candlestickList = new TreeSet<>(Comparator.comparing(BitfinexCandle::getTimestamp));

        // Snapshots contain multiple Bars, Updates only one
        if (payload.get(0) instanceof JSONArray) {
            for (int pos = 0; pos < payload.length(); pos++) {
                final JSONArray parts = payload.getJSONArray(pos);
                BitfinexCandle candlestick = jsonToCandlestick(parts);
                candlestickList.add(candlestick);
            }
        } else {
            BitfinexCandle candlestick = jsonToCandlestick(payload);
            candlestickList.add(candlestick);
        }
        candlesConsumer.accept(symbol, candlestickList);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    private BitfinexCandle jsonToCandlestick(final JSONArray parts) {
        // 0 = Timestamp, 1 = Open, 2 = Close, 3 = High, 4 = Low,  5 = Volume
        final long timestamp = parts.getLong(0);
        final BigDecimal open = parts.getBigDecimal(1);
        final BigDecimal close = parts.getBigDecimal(2);
        final BigDecimal high = parts.getBigDecimal(3);
        final BigDecimal low = parts.getBigDecimal(4);
        final BigDecimal volume = parts.getBigDecimal(5);

        return new BitfinexCandle(timestamp, open, close, high, low, Optional.of(volume));
    }

    /**
     * candlestick event consumer
     *
     * @param consumer of event
     */
    public void onCandlesticksEvent(BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> consumer) {
        this.candlesConsumer = consumer;
    }
}
