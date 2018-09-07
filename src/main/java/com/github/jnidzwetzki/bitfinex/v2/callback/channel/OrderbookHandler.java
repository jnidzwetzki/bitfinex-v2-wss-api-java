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

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class OrderbookHandler implements ChannelCallbackHandler {

    private BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> orderBookEntryConsumer = (sym, e) -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {
        // Example: [13182,1,-0.1]
        try {
            final BitfinexOrderBookSymbol config = (BitfinexOrderBookSymbol) channelSymbol;
            final List<BitfinexOrderBookEntry> entries = new ArrayList<>();

            // Snapshots contain multiple Orderbook entries, updates only one
            if (jsonArray.get(0) instanceof JSONArray) {
                for (int pos = 0; pos < jsonArray.length(); pos++) {
                    final JSONArray parts = jsonArray.getJSONArray(pos);
                    BitfinexOrderBookEntry entry = jsonToOrderBookEntry(parts);
                    entries.add(entry);
                }
            } else {
                BitfinexOrderBookEntry entry = jsonToOrderBookEntry(jsonArray);
                entries.add(entry);
            }
            orderBookEntryConsumer.accept(config, entries);
        } catch (JSONException e) {
            throw new APIException(e);
        }
    }

    private BitfinexOrderBookEntry jsonToOrderBookEntry(final JSONArray jsonArray) {
        final BigDecimal price = jsonArray.getBigDecimal(0);
        final Integer count = jsonArray.getInt(1);
        final BigDecimal amount = jsonArray.getBigDecimal(2);

        return new BitfinexOrderBookEntry(null, price, amount, count);
    }

    public void onOrderBookEvent(BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> consumer) {
        this.orderBookEntryConsumer = consumer;
    }
}
