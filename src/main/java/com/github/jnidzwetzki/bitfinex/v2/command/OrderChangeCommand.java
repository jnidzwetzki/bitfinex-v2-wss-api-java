package com.github.jnidzwetzki.bitfinex.v2.command;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
import org.json.JSONObject;

public class OrderChangeCommand implements BitfinexOrderCommand {

    private final BitfinexSubmittedOrder bitfinexOrder;

    public OrderChangeCommand(final BitfinexSubmittedOrder bitfinexOrder) {
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
