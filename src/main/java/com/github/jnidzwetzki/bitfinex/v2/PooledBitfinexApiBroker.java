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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.util.EventsInTimeslotManager;

/**
 * BitfinexApiBroker client spreading amount of channels across multiple websocket connections.
 */
public class PooledBitfinexApiBroker implements BitfinexWebsocketClient {

    private final AtomicInteger numberOfClients = new AtomicInteger(0);
    private final Map<Integer, BitfinexWebsocketClient> clients = new ConcurrentHashMap<>();
    private final Map<BitfinexWebsocketClient, Set<BitfinexStreamSymbol>> pendingSubscriptions = new ConcurrentHashMap<>();

    private final BitfinexWebsocketConfiguration configuration;
    private final BitfinexApiCallbackRegistry callbackRegistry;
    private final SequenceNumberAuditor sequenceNumberAuditor;
    private final int maxChannelsPerClient;

    private final EventsInTimeslotManager connectEventManager;

    private final QuoteManager quoteManager;
    private final OrderbookManager orderbookManager;
    private final RawOrderbookManager rawOrderbookManager;
    private final OrderManager orderManager;
    private final TradeManager tradeManager;
    private final PositionManager positionManager;
    private final WalletManager walletManager;
    private final ConnectionFeatureManager connectionFeatureManager;

    public PooledBitfinexApiBroker(final BitfinexWebsocketConfiguration config, final BitfinexApiCallbackRegistry callbacks,
                                   final SequenceNumberAuditor seqNoAuditor, final int channelsPerConnection) {
    		
        configuration = new BitfinexWebsocketConfiguration(config);
        callbackRegistry = callbacks;
        sequenceNumberAuditor = seqNoAuditor;
        maxChannelsPerClient = channelsPerConnection;

        connectEventManager = new EventsInTimeslotManager(1, configuration.getConnectionEstablishingDelay(), TimeUnit.MILLISECONDS);

        quoteManager = new QuoteManager(this, configuration.getExecutorService());
        orderbookManager = new OrderbookManager(this, configuration.getExecutorService());
        rawOrderbookManager = new RawOrderbookManager(this, configuration.getExecutorService());
        orderManager = new OrderManager(this, configuration.getExecutorService());
        tradeManager = new TradeManager(this, configuration.getExecutorService());
        positionManager = new PositionManager(this, configuration.getExecutorService());
        walletManager = new WalletManager(this, configuration.getExecutorService());
        connectionFeatureManager = new ConnectionFeatureManager(this, configuration.getExecutorService());

        callbackRegistry.onSubscribeChannelEvent(sym -> pendingSubscriptions.forEach((client, symbols) -> symbols.remove(sym)));
        callbackRegistry.onUnsubscribeChannelEvent(sym -> pendingSubscriptions.forEach((client, symbols) -> symbols.remove(sym)));

        SimpleBitfinexApiBroker authClient = new SimpleBitfinexApiBroker(configuration, callbackRegistry, seqNoAuditor);
        clients.put(numberOfClients.getAndIncrement(), authClient);
        pendingSubscriptions.put(authClient, ConcurrentHashMap.newKeySet());
    }

    @Override
    public void connect() {
        clients.values().forEach(BitfinexWebsocketClient::connect);
    }

    @Override
    public void close() {
        clients.values().forEach(BitfinexWebsocketClient::close);
    }

    @Override
    public void sendCommand(BitfinexCommand command) {
        BitfinexWebsocketClient client = clients.get(0);
        if (command instanceof SubscribeCommand) {
            BitfinexStreamSymbol symbol = ((SubscribeCommand) command).getSymbol();
            synchronized (this) {
                client = clients.values().stream()
                        .filter(c -> {
                            int subscribed = c.getSubscribedChannels().size();
                            int pending = pendingSubscriptions.get(c).size();
                            return subscribed + pending < maxChannelsPerClient;
                        })
                        .findFirst().orElseGet(this::createAndConnectClient);
                pendingSubscriptions.get(client).add(symbol);
            }
        }
        if (command instanceof UnsubscribeChannelCommand) {
            UnsubscribeChannelCommand unsubscribeCommand = (UnsubscribeChannelCommand) command;
            BitfinexStreamSymbol symbol = unsubscribeCommand.getSymbol();
            client = clients.values().stream()
                    .filter(c -> c.getSubscribedChannels().contains(symbol))
                    .findFirst().orElseThrow(IllegalStateException::new);
        }
        client.sendCommand(command);
    }

    public int websocketConnCount() {
        return numberOfClients.get();
    }

    @Override
    public boolean reconnect() {
        boolean retVal = false;
        for (BitfinexWebsocketClient client : clients.values()) {
            retVal |= client.reconnect();
        }
        return retVal;
    }

    @Override
    public boolean unsubscribeAllChannels() {
        boolean retVal = true;
        for (BitfinexWebsocketClient client : clients.values()) {
            retVal &= client.unsubscribeAllChannels();
        }
        return retVal;
    }

    @Override
    public boolean isAuthenticated() {
        boolean retVal = false;
        for (BitfinexWebsocketClient client : clients.values()) {
            retVal |= client.isAuthenticated();
        }
        return retVal;
    }

    @Override
    public BitfinexApiKeyPermissions getApiKeyPermissions() {
        return clients.get(0).getApiKeyPermissions();
    }

    @Override
    public Collection<BitfinexStreamSymbol> getSubscribedChannels() {
        return clients.values().stream()
                .flatMap(c -> c.getSubscribedChannels().stream())
                .collect(Collectors.toList());
    }

    @Override
    public BitfinexWebsocketConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public BitfinexApiCallbackListeners getCallbacks() {
        return callbackRegistry;
    }

    @Override
    public QuoteManager getQuoteManager() {
        return quoteManager;
    }

    @Override
    public OrderbookManager getOrderbookManager() {
        return orderbookManager;
    }

    @Override
    public RawOrderbookManager getRawOrderbookManager() {
        return rawOrderbookManager;
    }

    @Override
    public PositionManager getPositionManager() {
        return positionManager;
    }

    @Override
    public OrderManager getOrderManager() {
        return orderManager;
    }

    @Override
    public TradeManager getTradeManager() {
        return tradeManager;
    }

    @Override
    public WalletManager getWalletManager() {
        return walletManager;
    }

    @Override
    public ConnectionFeatureManager getConnectionFeatureManager() {
        return connectionFeatureManager;
    }

    private BitfinexWebsocketClient createAndConnectClient() {
        BitfinexWebsocketConfiguration config = new BitfinexWebsocketConfiguration(configuration);
        config.setAuthenticationEnabled(false);
        config.setManagersActive(false);
        SimpleBitfinexApiBroker client = new SimpleBitfinexApiBroker(config, callbackRegistry, sequenceNumberAuditor);
        clients.put(numberOfClients.getAndIncrement(), client);
        if (connectEventManager.getNumberOfEventsInTimeslot() > 1) {
            try {
                connectEventManager.waitForNewTimeslot();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        connectEventManager.recordNewEvent();
        pendingSubscriptions.put(client, ConcurrentHashMap.newKeySet());
        client.connect();
        return client;
    }
}
