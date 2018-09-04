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
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class RawOrderbookHandler implements ChannelCallbackHandler {

    private BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>> orderbookEntryConsumer = (c, e) -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {
        // Example: [13182,1,-0.1]
        try {
            final RawOrderbookConfiguration config = (RawOrderbookConfiguration) channelSymbol;
            final List<RawOrderbookEntry> entries = new ArrayList<>();

            // Snapshots contain multiple Orderbook entries, updates only one
            if (jsonArray.get(0) instanceof JSONArray) {
                for (int pos = 0; pos < jsonArray.length(); pos++) {
                    final JSONArray part = jsonArray.getJSONArray(pos);
                    RawOrderbookEntry entry = jsonToRawOrderbookEntry(part);
                    entries.add(entry);
                }
            } else {
                RawOrderbookEntry entry = jsonToRawOrderbookEntry(jsonArray);
                entries.add(entry);
            }
            orderbookEntryConsumer.accept(config, entries);
        } catch (JSONException e) {
            throw new APIException(e);
        }
    }

    private RawOrderbookEntry jsonToRawOrderbookEntry(final JSONArray jsonArray) {
        final long orderId = jsonArray.getNumber(0).longValue();
        final BigDecimal price = jsonArray.getBigDecimal(1);
        final BigDecimal amount = jsonArray.getBigDecimal(2);

        return new RawOrderbookEntry(orderId, price, amount);
    }

    public void onOrderbookEvent(BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>> consumer) {
        this.orderbookEntryConsumer = consumer;
    }

}
