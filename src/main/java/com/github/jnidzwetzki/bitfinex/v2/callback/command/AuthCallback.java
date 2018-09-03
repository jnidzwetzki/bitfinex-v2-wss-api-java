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

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;

public class AuthCallback implements CommandCallbackHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthCallback.class);

    private Consumer<ConnectionCapabilities> authSuccessConsumer = c -> {};
    private Consumer<ConnectionCapabilities> authFailedConsumer = c -> {};

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChannelData(final JSONObject jsonObject)
            throws APIException {
        final String status = jsonObject.getString("status");
        if (status.equals("OK")) {
            LOGGER.info("authentication successful");
            authSuccessConsumer.accept(new ConnectionCapabilities(jsonObject));
        } else {
            LOGGER.error("Unable to authenticate: {}", jsonObject.toString());
            authFailedConsumer.accept(ConnectionCapabilities.NO_CAPABILITIES);
        }
    }

    /**
     * successful authentication event consumer
     *
     * @param consumer of event
     */
    public void onAuthenticationSuccessEvent(Consumer<ConnectionCapabilities> consumer) {
        this.authSuccessConsumer = consumer;
    }

    /**
     * failed authentication event consumer
     * @param consumer of event
     */
    public void onAuthenticationFailedEvent(Consumer<ConnectionCapabilities> consumer) {
        this.authFailedConsumer = consumer;
    }
}
