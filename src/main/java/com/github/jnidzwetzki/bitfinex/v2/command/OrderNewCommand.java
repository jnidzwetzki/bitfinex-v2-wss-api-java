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
package com.github.jnidzwetzki.bitfinex.v2.command;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;

public class OrderNewCommand implements BitfinexOrderCommand {

    private final BitfinexOrder bitfinexOrder;

    public OrderNewCommand(final BitfinexOrder bitfinexOrder) {
        this.bitfinexOrder = bitfinexOrder;
    }

    @Override
    public String getCommand(final BitfinexWebsocketClient client) throws BitfinexCommandException {
        final JSONObject orderJson = new JSONObject();
        orderJson.put("type", bitfinexOrder.getOrderType().getBifinexString());
        orderJson.put("symbol", bitfinexOrder.getCurrencyPair().toBitfinexString());
        orderJson.put("amount", bitfinexOrder.getAmount().toString());
        if (bitfinexOrder.getPrice() != null) {
            orderJson.put("price", bitfinexOrder.getPrice().toString());
        }
        if (bitfinexOrder.getPriceTrailing() != null) {
            orderJson.put("price_trailing", bitfinexOrder.getPriceTrailing().toString());
        }
        if (bitfinexOrder.getPriceAuxLimit() != null) {
            orderJson.put("price_aux_limit", bitfinexOrder.getPriceAuxLimit().toString());
        }
        if (bitfinexOrder.getPriceOcoStop() != null) {
            orderJson.put("price_oco_stop", bitfinexOrder.getPriceOcoStop().toString());
        }
        if (!bitfinexOrder.getOrderFlags().isEmpty()) {
            orderJson.put("flags", bitfinexOrder.getCombinedFlags());
        }
        orderJson.put("cid", bitfinexOrder.getClientId());
        bitfinexOrder.getClientGroupId().ifPresent(groupId -> orderJson.put("gid", bitfinexOrder.getClientGroupId().get()));
        if (bitfinexOrder.getAffiliateCode() != null) {
            orderJson.put("meta", getMetaJson());
        }
        return "[0, \"on\", null, " + orderJson.toString() + "]";
    }

    private JSONObject getMetaJson() {
        final JSONObject metaJson = new JSONObject();
        metaJson.put("aff_code", bitfinexOrder.getAffiliateCode());
        return metaJson;
    }

}
