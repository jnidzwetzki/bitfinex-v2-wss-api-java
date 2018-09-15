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
package com.github.jnidzwetzki.bitfinex.v2.callback.command;

import java.util.function.Consumer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;

public class AuthCallback implements CommandCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(AuthCallback.class);

    private Consumer<BitfinexApiKeyPermissions> authSuccessConsumer = c -> {};
    private Consumer<BitfinexApiKeyPermissions> authFailedConsumer = c -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final JSONObject jsonObject)
            throws BitfinexClientException {
        final String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            logger.info("authentication successful");
            BitfinexApiKeyPermissions permissions = jsonToBitfinexApiKeyPermissions(jsonObject);
            authSuccessConsumer.accept(permissions);
        } else {
            logger.error("Unable to authenticate: {}", jsonObject.toString());
            authFailedConsumer.accept(BitfinexApiKeyPermissions.NO_PERMISSIONS);
        }
    }

    /**
     * successful authentication event consumer
     *
     * @param consumer of event
     */
    public void onAuthenticationSuccessEvent(Consumer<BitfinexApiKeyPermissions> consumer) {
        this.authSuccessConsumer = consumer;
    }

    /**
     * failed authentication event consumer
     * @param consumer of event
     */
    public void onAuthenticationFailedEvent(Consumer<BitfinexApiKeyPermissions> consumer) {
        this.authFailedConsumer = consumer;
    }

    private BitfinexApiKeyPermissions jsonToBitfinexApiKeyPermissions(final JSONObject jsonObject) {
        final JSONObject caps = jsonObject.getJSONObject("caps");
        JSONObject orders = caps.getJSONObject("orders");
        boolean orderReadPermission = orders.getInt("read") == 1;
        boolean orderWritePermission = orders.getInt("write") == 1;
        JSONObject account = caps.getJSONObject("account");
        boolean accountReadPermission = account.getInt("read") == 1;
        boolean accountWritePermission = account.getInt("write") == 1;
        JSONObject funding = caps.getJSONObject("funding");
        boolean fundingReadPermission = funding.getInt("read") == 1;
        boolean fundingWritePermission = funding.getInt("write") == 1;
        JSONObject history = caps.getJSONObject("history");
        boolean historyReadPermission = history.getInt("read") == 1;
        boolean historyWritePermission = history.getInt("write") == 1;
        JSONObject wallets = caps.getJSONObject("wallets");
        boolean walletsReadPermission = wallets.getInt("read") == 1;
        boolean walletsWritePermission = wallets.getInt("write") == 1;
        JSONObject withdraw = caps.getJSONObject("withdraw");
        boolean withdrawReadPermission = withdraw.getInt("read") == 1;
        boolean withdrawWritePermission = withdraw.getInt("write") == 1;
        JSONObject positions = caps.getJSONObject("positions");
        boolean positionReadPermission = positions.getInt("read") == 1;
        boolean positionWritePermission = positions.getInt("write") == 1;

        return new BitfinexApiKeyPermissions(orderReadPermission, orderWritePermission,
                accountReadPermission, accountWritePermission, fundingReadPermission,
                fundingWritePermission, historyReadPermission, historyWritePermission,
                walletsReadPermission, walletsWritePermission, withdrawReadPermission,
                withdrawWritePermission, positionReadPermission, positionWritePermission);
    }
}
