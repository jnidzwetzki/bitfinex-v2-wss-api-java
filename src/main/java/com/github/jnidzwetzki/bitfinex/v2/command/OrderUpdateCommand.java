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
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;

public class OrderUpdateCommand implements BitfinexOrderCommand {
	
    private final BitfinexSubmittedOrder bitfinexOrder;

    public OrderUpdateCommand(final BitfinexSubmittedOrder bitfinexOrder) {
        this.bitfinexOrder = bitfinexOrder;
    }

    @Override
    public String getCommand(final BitfinexWebsocketClient client) throws BitfinexCommandException {
        final JSONObject orderJson = new JSONObject();
        orderJson.put("id", bitfinexOrder.getOrderId());
        
        if (bitfinexOrder.getAmount() != null) {
            orderJson.put("amount", bitfinexOrder.getAmount().toString());
        }
        
        if (bitfinexOrder.getPrice() != null) {
            orderJson.put("price", bitfinexOrder.getPrice().toString());
        }

        orderJson.put("cid", bitfinexOrder.getClientId());
        bitfinexOrder.getClientGroupId().ifPresent(groupId -> orderJson.put("gid", bitfinexOrder.getClientGroupId().get()));
        return "[0, \"ou\", null, " + orderJson.toString() + "]";
    }
}
