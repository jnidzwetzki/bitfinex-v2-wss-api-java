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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;

public class PositionHandler implements APICallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(PositionHandler.class);
    private Consumer<Collection<Position>> positionConsumer = p -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final JSONArray message) throws APIException {
        logger.info("Got position callback {}", message.toString());
        final JSONArray json = message.getJSONArray(2);

        ArrayList<Position> positions = Lists.newArrayList();
        // No positions active
        if (json.length() == 0) {
            positionConsumer.accept(positions);
            return;
        }

        if (json.get(0) instanceof JSONArray) {
            // snapshot
            for (int orderPos = 0; orderPos < json.length(); orderPos++) {
                final JSONArray orderArray = json.getJSONArray(orderPos);
                Position position = jsonArrayToPosition(orderArray);
                positions.add(position);
            }
        } else {
            // update
            Position position = jsonArrayToPosition(json);
            positions.add(position);
        }
        positionConsumer.accept(positions);
    }

    private Position jsonArrayToPosition(final JSONArray json) {
        final String currencyString = json.getString(0);
        final BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString(currencyString);

        final Position position = new Position(currency);
        position.setStatus(json.getString(1));
        position.setAmount(json.getBigDecimal(2));
        position.setBasePrice(json.getBigDecimal(3));
        position.setMarginFunding(json.getBigDecimal(4));
        position.setMarginFundingType(json.optInt(5, -1));
        position.setPl(json.optBigDecimal(6, null));
        position.setPlPercent(json.optBigDecimal(7, null));
        position.setPriceLiquidation(json.optBigDecimal(8, null));
        position.setLeverage(json.optBigDecimal(9, null));

        return position;
    }

    /**
     * positions event consumer
     * @param consumer of event
     */
    public void onPositionsEvent(Consumer<Collection<Position>> consumer) {
        this.positionConsumer = consumer;
    }
}
