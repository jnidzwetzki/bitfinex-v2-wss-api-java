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
package com.github.jnidzwetzki.bitfinex.v2;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.google.common.util.concurrent.MoreExecutors;

import com.github.jnidzwetzki.bitfinex.v2.commands.AuthCommand;

public class BitfinexApiBrokerConfig {

    private String apiKey;
    private String apiSecret;
    private boolean authenticationEnabled = false;
    private boolean heartbeatThreadActive = true;
    private boolean deadmanSwitchActive = false;
    private boolean managersActive = true;
    private Supplier<String> authNonceProducer = AuthCommand.AUTH_NONCE_PRODUCER_TIMESTAMP;
    private ExecutorService executorService = MoreExecutors.newDirectExecutorService();

    public BitfinexApiBrokerConfig() {

    }

    public BitfinexApiBrokerConfig(final BitfinexApiBrokerConfig copy) {
        this.apiKey = copy.apiKey;
        this.apiSecret = copy.apiSecret;
        this.authenticationEnabled = copy.authenticationEnabled;
        this.heartbeatThreadActive = copy.heartbeatThreadActive;
        this.deadmanSwitchActive = copy.deadmanSwitchActive;
        this.managersActive = copy.managersActive;
        this.authNonceProducer = copy.authNonceProducer;
        this.executorService = copy.executorService;
    }

    public void setApiCredentials(final String apiKey, final String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.authenticationEnabled = true;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    public void setAuthenticationEnabled(final boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public boolean isHeartbeatThreadActive() {
        return heartbeatThreadActive;
    }

    public void setHeartbeatThreadActive(final boolean heartbeatThreadActive) {
        this.heartbeatThreadActive = heartbeatThreadActive;
    }

    public boolean isDeadmanSwitchActive() {
        return deadmanSwitchActive;
    }

    public void setDeadmanSwitchActive(final boolean deadmanSwitchActive) {
        this.deadmanSwitchActive = deadmanSwitchActive;
    }

    public boolean isManagersActive() {
        return managersActive;
    }

    public void setManagersActive(final boolean managersActive) {
        this.managersActive = managersActive;
    }

    public Supplier<String> getAuthNonceProducer() {
        return authNonceProducer;
    }

    public void setAuthNonceProducer(final Supplier<String> authNonceProducer) {
        this.authNonceProducer = authNonceProducer;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }
}
