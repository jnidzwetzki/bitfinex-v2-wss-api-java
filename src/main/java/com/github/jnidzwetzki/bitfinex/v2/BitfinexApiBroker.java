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

import java.io.Closeable;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.AccountInfoHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ExecutedTradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.OrderbookHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.RawOrderbookHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.TickHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.AuthCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.CommandCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ConfCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ConnectionHeartbeatCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.DoNothingCommandCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ErrorCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.SubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.UnsubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.command.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.exception.CommandException;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;
import com.github.jnidzwetzki.bitfinex.v2.util.BitfinexStreamSymbolToChannelIdResolverAware;

public class BitfinexApiBroker implements Closeable {

	/**
	 * The bitfinex api
	 */
	public final static String BITFINEX_URI = "wss://api.bitfinex.com/ws/2";

	/**
	 * broker configuration
	 */
	private final BitfinexApiBrokerConfig configuration;

	/**
	 * callback registry
	 */
	private BitfinexApiCallbackRegistry callbackRegistry;

	/**
	 * The websocket endpoint
	 */
	private WebsocketClientEndpoint websocketEndpoint;

	/**
	 * The channel map
	 */
	private final Map<Integer, ChannelCallbackHandler> channelIdToHandlerMap;
	
	/**
	 * The tick manager
	 */
	private final QuoteManager quoteManager;

	/**
	 * The trading orderbook manager
	 */
	private final OrderbookManager orderbookManager;
	
	/**
	 * The trading RAW orderbook manager
	 */
	private final RawOrderbookManager rawOrderbookManager;
	
	/**
	 * The position manager
	 */
	private final PositionManager positionManager;
	
	/**
	 * The order manager
	 */
	private final OrderManager orderManager;
	
	/**
	 * The trade manager
	 */
	private final TradeManager tradeManager;
	
	/**
	 * The wallet manager
	 */
	private final WalletManager walletManager;
	
	/**
	 * The connection feature manager
	 */
	private final ConnectionFeatureManager connectionFeatureManager;
	
	/**
	 * The last heartbeat value
	 */
	private final AtomicLong lastHeartbeat;

	/**
	 * The heartbeat thread
	 */
	private Thread heartbeatThread;

	/**
	 * The permissions of the connection
	 */
	private BitfinexApiKeyPermissions permissions;
	
	/**
	 * Is the connection authenticated?
	 */
	private boolean authenticated;

	/**
	 * The command callbacks
	 */
	private Map<String, CommandCallbackHandler> commandCallbacks;
	
	/**
	 * The sequence number auditor
	 */
	private final SequenceNumberAuditor sequenceNumberAuditor;

	private final static Logger logger = LoggerFactory.getLogger(BitfinexApiBroker.class);

	public BitfinexApiBroker(BitfinexApiBrokerConfig config) {
		this(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
	}

	public BitfinexApiBroker(BitfinexApiBrokerConfig config, BitfinexApiCallbackRegistry callbackRegistry, SequenceNumberAuditor sequenceNumberAuditor) {
		this.configuration = new BitfinexApiBrokerConfig(config);
		this.callbackRegistry = callbackRegistry;

		this.channelIdToHandlerMap = new ConcurrentHashMap<>();
		this.permissions = BitfinexApiKeyPermissions.NO_PERMISSIONS;
		this.sequenceNumberAuditor = sequenceNumberAuditor;
		this.lastHeartbeat = new AtomicLong(0);
		this.quoteManager = new QuoteManager(this, configuration.getExecutorService());
		this.orderbookManager = new OrderbookManager(this, configuration.getExecutorService());
		this.rawOrderbookManager = new RawOrderbookManager(this, configuration.getExecutorService());
		this.orderManager = new OrderManager(this, configuration.getExecutorService());
		this.tradeManager = new TradeManager(this, configuration.getExecutorService());
		this.positionManager = new PositionManager(this, configuration.getExecutorService());
		this.walletManager = new WalletManager(this, configuration.getExecutorService());
		this.connectionFeatureManager = new ConnectionFeatureManager(this, configuration.getExecutorService());
        setupCommandCallbacks();
	}

	/**
	 * Setup the command callbacks
	 */
	private void setupCommandCallbacks() {
		commandCallbacks = new HashMap<>();
		commandCallbacks.put("info", new DoNothingCommandCallback());

		// TODO: hb is not ping:pong
		final ConnectionHeartbeatCallback pong = new ConnectionHeartbeatCallback();
		pong.onHeartbeatEvent(l -> this.updateConnectionHeartbeat());
		commandCallbacks.put("pong", pong);

		final SubscribedCallback subscribed = new SubscribedCallback();
		subscribed.onSubscribedEvent((channelId, symbol) -> {
			synchronized (channelIdToHandlerMap) {
				ChannelCallbackHandler channelCallbackHandler = createChannelCallbackHandler(channelId, symbol);
				channelIdToHandlerMap.put(channelId, channelCallbackHandler);
				channelIdToHandlerMap.notifyAll();
			}
			callbackRegistry.acceptSubscribeChannelEvent(symbol);
		});
		commandCallbacks.put("subscribed", subscribed);

		UnsubscribedCallback unsubscribed = new UnsubscribedCallback();
		unsubscribed.onUnsubscribedChannelEvent(channelId -> {
			ChannelCallbackHandler removed;
			synchronized (channelIdToHandlerMap) {
				removed = channelIdToHandlerMap.remove(channelId);
				channelIdToHandlerMap.notifyAll();
			}
			if (removed != null) {
				callbackRegistry.acceptUnsubscribeChannelEvent(removed.getSymbol());
			}
		});
		commandCallbacks.put("unsubscribed", unsubscribed);

		final AuthCallback auth = new AuthCallback();
		auth.onAuthenticationSuccessEvent(permissions -> {
			BitfinexAccountSymbol symbol = BitfinexSymbols.account(configuration.getApiKey(), permissions);
			AccountInfoHandler handler = new AccountInfoHandler(0, symbol);
			handler.onHeartbeatEvent(timestamp -> this.updateConnectionHeartbeat());
			handler.onPositionsEvent(callbackRegistry::acceptPositionsEvent);
			handler.onWalletsEvent(callbackRegistry::acceptWalletsEvent);
			handler.onSubmittedOrderEvent(callbackRegistry::acceptSubmittedOrderEvent);
			handler.onTradeEvent(callbackRegistry::acceptTradeEvent);
			handler.onOrderNotification(callbackRegistry::acceptOrderNotification);

			channelIdToHandlerMap.put(0, handler);
			callbackRegistry.acceptAuthenticationSuccessEvent(symbol);
		});
		auth.onAuthenticationFailedEvent(permissions -> {
			BitfinexAccountSymbol symbol = BitfinexSymbols.account(configuration.getApiKey(), permissions);
			callbackRegistry.acceptAuthenticationFailedEvent(symbol);
		});
		commandCallbacks.put("auth", auth);

		final ConfCallback conf = new ConfCallback();
		conf.onConnectionFeatureEvent(connectionFeatureManager::setActiveConnectionFeatures);
		commandCallbacks.put("conf", conf);
		commandCallbacks.put("error", new ErrorCallback());
	}

	/**
	 * Open the connection
	 * @throws APIException
	 */
	public void connect() throws APIException {
		try {
			sequenceNumberAuditor.reset();
			CountDownLatch connectionReadyLatch = new CountDownLatch(4);

			Closeable authSuccessEventCallback = callbackRegistry.onAuthenticationSuccessEvent(c -> {
				permissions = c.getPermissions();
				authenticated = true;
				connectionReadyLatch.countDown();
			});
			Closeable authFailedCallback = callbackRegistry.onAuthenticationFailedEvent(c -> {
				permissions = c.getPermissions();
				authenticated = false;
				while (connectionReadyLatch.getCount() != 0) {
					connectionReadyLatch.countDown();
				}
			});
			Closeable positionInitCallback = callbackRegistry.onPositionsEvent((a, p) -> connectionReadyLatch.countDown());
			Closeable walletsInitCallback = callbackRegistry.onWalletsEvent((a, w) -> connectionReadyLatch.countDown());
			Closeable orderInitCallback = callbackRegistry.onSubmittedOrderEvent((a, o) -> connectionReadyLatch.countDown());

			websocketEndpoint = new WebsocketClientEndpoint(new URI(BITFINEX_URI), this::websocketCallback);
			websocketEndpoint.connect();
			updateConnectionHeartbeat();

			connectionFeatureManager.applyConnectionFeatures();
			if( configuration.isAuthenticationEnabled()) {
				authenticateAndWait(connectionReadyLatch);
			}

			authSuccessEventCallback.close();
			authFailedCallback.close();
			positionInitCallback.close();
			walletsInitCallback.close();
			orderInitCallback.close();

			if (configuration.isHeartbeatThreadActive()) {
				heartbeatThread = new Thread(new HeartbeatThread(this, websocketEndpoint, lastHeartbeat::get));
				heartbeatThread.start();
			}
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	/**
	 * Disconnect the websocket
	 */
	@Override
	public void close() {
		if (heartbeatThread != null) {
			heartbeatThread.interrupt();
			heartbeatThread = null;
		}

		if (websocketEndpoint != null) {
			websocketEndpoint.close();
			websocketEndpoint = null;
		}
	}

	/**
	 * Send a new API command
	 * @param apiCommand
	 */
	public void sendCommand(final BitfinexCommand apiCommand) {
		try {
			if (apiCommand instanceof BitfinexStreamSymbolToChannelIdResolverAware) {
				BitfinexStreamSymbolToChannelIdResolverAware aware = (BitfinexStreamSymbolToChannelIdResolverAware) apiCommand;
				aware.setResolver(symbol -> {
					final Integer channelId = getChannelForSymbol(symbol);
					if (channelId == null) {
						throw new IllegalArgumentException("Unknown symbol: " + symbol);
					}
					return channelId;
				});
			}
			final String command = apiCommand.getCommand(this);
			logger.debug("Sending to server: {}", command);
			websocketEndpoint.sendMessage(command);
		} catch (CommandException e) {
			logger.error("Got Exception while sending command", e);
		}
	}

	/**
	 * Perform a reconnect
	 * @return
	 */
	public synchronized boolean reconnect() {
		try {
			logger.info("Performing reconnect");
			websocketEndpoint.close();

			permissions = BitfinexApiKeyPermissions.NO_PERMISSIONS;
			authenticated = false;
			sequenceNumberAuditor.reset();
			connectionFeatureManager.setActiveConnectionFeatures(0);

			// Invalidate old data
			quoteManager.invalidateTickerHeartbeat();
			orderManager.clear();
			positionManager.clear();

			CountDownLatch connectionReadyLatch = new CountDownLatch(4);

			Closeable authSuccessEventCallback = callbackRegistry.onAuthenticationSuccessEvent(c -> {
				permissions = c.getPermissions();
				authenticated = true;
				connectionReadyLatch.countDown();
			});
			Closeable authFailedCallback = callbackRegistry.onAuthenticationFailedEvent(c -> {
				permissions = c.getPermissions();
				authenticated = false;
				while (connectionReadyLatch.getCount() != 0) {
					connectionReadyLatch.countDown();
				}
			});
			Closeable positionInitCallback = callbackRegistry.onPositionsEvent((a, p) -> connectionReadyLatch.countDown());
			Closeable walletsInitCallback = callbackRegistry.onWalletsEvent((a, w) -> connectionReadyLatch.countDown());
			Closeable orderInitCallback = callbackRegistry.onSubmittedOrderEvent((a, o) -> connectionReadyLatch.countDown());

			websocketEndpoint.connect();

			connectionFeatureManager.applyConnectionFeatures();
			if( configuration.isAuthenticationEnabled()) {
				authenticateAndWait(connectionReadyLatch);
			}
			authSuccessEventCallback.close();
			authFailedCallback.close();
			positionInitCallback.close();
			walletsInitCallback.close();
			orderInitCallback.close();

			resubscribeChannels();

			updateConnectionHeartbeat();

			return true;
		} catch (Exception e) {
			logger.error("Got exception while reconnect", e);
			websocketEndpoint.close();
			return false;
		}
	}

	private ChannelCallbackHandler createChannelCallbackHandler(int channelId, BitfinexStreamSymbol symbol) {
		if (symbol instanceof BitfinexCandlestickSymbol) {
			final CandlestickHandler handler = new CandlestickHandler(channelId, (BitfinexCandlestickSymbol) symbol);
			handler.onCandlesticksEvent(callbackRegistry::acceptCandlesticksEvent);
			return handler;
		} else if (symbol instanceof BitfinexOrderBookSymbol) {
			BitfinexOrderBookSymbol orderBookSymbol = (BitfinexOrderBookSymbol) symbol;
			if (orderBookSymbol.isRawOrderBook()) {
				final RawOrderbookHandler rawOrderBookHandler = new RawOrderbookHandler(channelId, orderBookSymbol);
				rawOrderBookHandler.onOrderbookEvent(callbackRegistry::acceptRawOrderBookEvent);
				return rawOrderBookHandler;
			} else {
				final OrderbookHandler orderbookHandler = new OrderbookHandler(channelId, orderBookSymbol);
				orderbookHandler.onOrderBookEvent(callbackRegistry::acceptOrderBookEvent);
				return orderbookHandler;
			}
		} else if (symbol instanceof BitfinexTickerSymbol) {
			final TickHandler handler = new TickHandler(channelId, (BitfinexTickerSymbol) symbol);
			handler.onTickEvent(callbackRegistry::acceptTickEvent);
			return handler;
		} else if (symbol instanceof BitfinexExecutedTradeSymbol) {
			final ExecutedTradeHandler handler = new ExecutedTradeHandler(channelId, (BitfinexExecutedTradeSymbol) symbol);
			handler.onExecutedTradeEvent(callbackRegistry::acceptExecutedTradeEvent);
			return handler;
		} else {
			throw new IllegalArgumentException("Cannot handle symbol: " + symbol);
		}
	}

	/**
	 * Execute the authentication and wait until the socket is ready
	 * @throws InterruptedException
	 * @throws APIException
	 */
	private void authenticateAndWait(CountDownLatch latch) throws InterruptedException, APIException {
		if (authenticated) {
			return;
		}
		sendCommand(new AuthCommand(configuration.getAuthNonceProducer()));
		logger.debug("Waiting for connection ready events");
		latch.await(10, TimeUnit.SECONDS);

		if (!authenticated) {
			throw new APIException("Unable to perform authentication, permissions are: " + permissions);
		}
	}

	/**
	 * We received a websocket callback
	 * @param message
	 */
	private void websocketCallback(final String message) {
		logger.debug("Got message: {}", message);

		if(message.startsWith("{")) {
			handleCommandCallback(message);
		} else if(message.startsWith("[")) {
			handleChannelCallback(message);
		} else {
			logger.error("Got unknown callback: {}", message);
		}
	}

	/**
	 * Handle a command callback
	 */
	private void handleCommandCallback(final String message) {
		logger.debug("Got {}", message);
		// JSON callback
        final JSONObject jsonObject = new JSONObject(message);
		final String eventType = jsonObject.getString("event");

		CommandCallbackHandler commandCallbackHandler = commandCallbacks.get(eventType);
		if( commandCallbackHandler == null ) {
			logger.error("Unknown event: {}", message);
			return;
		}
		try {
			commandCallbackHandler.handleChannelData(jsonObject);
		} catch (APIException e) {
			logger.error("Got an exception while handling callback");
		}
	}

	private void updateConnectionHeartbeat() {
		lastHeartbeat.set(System.currentTimeMillis());
	}

	/**
	 * Handle a channel callback
	 * @param message
	 */
	private void handleChannelCallback(final String message) {
		// Channel callback
		logger.debug("Channel callback");
		updateConnectionHeartbeat();

		// JSON callback
		final JSONArray jsonArray = new JSONArray(new JSONTokener(message));

		if(connectionFeatureManager.isConnectionFeatureActive(BitfinexConnectionFeature.SEQ_ALL)) {
			sequenceNumberAuditor.auditPackage(jsonArray);
		}

		final int channel = jsonArray.getInt(0);
		ChannelCallbackHandler channelCallbackHandler = channelIdToHandlerMap.get(channel);
		if (channelCallbackHandler == null) {
			logger.error("Unable to determine symbol for channel {} / data is {} ", channel, jsonArray);
			reconnect();
			return;
		}
		String action = null;
		final JSONArray payload;
		if (jsonArray.get(1) instanceof String) {
			action = jsonArray.getString(1);
			payload = jsonArray.optJSONArray(2);
		} else {
			payload = jsonArray.optJSONArray(1);
		}
		if(Objects.equals(action, "hb")) {
			quoteManager.updateChannelHeartbeat(channelCallbackHandler.getSymbol());
		}
		try {
			if (payload == null) {
				return;
			}
			channelCallbackHandler.handleChannelData(action, payload);
		} catch (APIException e) {
			logger.error("Got exception while handling callback", e);
		}
	}

	/**
	 * Find the channel for the given symbol
	 * @param symbol
	 * @return
	 */
	private Integer getChannelForSymbol(final BitfinexStreamSymbol symbol) {
		synchronized (channelIdToHandlerMap) {
			return channelIdToHandlerMap.values()
					.stream()
					.filter(v -> Objects.equals(v.getSymbol(), symbol))
					.map(ChannelCallbackHandler::getChannelId)
					.findFirst().orElse(null);
		}
	}

	/**
	 * Re-subscribe the old ticker
	 * @throws InterruptedException
	 * @throws APIException
	 */
	private void resubscribeChannels() throws InterruptedException, APIException {
		final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap = new HashMap<>();

		synchronized (channelIdToHandlerMap) {
			oldChannelIdSymbolMap.putAll(channelIdToHandlerMap);
			channelIdToHandlerMap.clear();
			channelIdToHandlerMap.notifyAll();
		}
		
		// Resubscribe channels
		for(ChannelCallbackHandler handler : oldChannelIdSymbolMap.values()) {
			BitfinexStreamSymbol symbol = handler.getSymbol();
			if(symbol instanceof BitfinexTickerSymbol) {
				sendCommand(new SubscribeTickerCommand((BitfinexTickerSymbol) symbol));
			} else if(symbol instanceof BitfinexExecutedTradeSymbol) {
				sendCommand(new SubscribeTradesCommand((BitfinexExecutedTradeSymbol) symbol));
			} else if(symbol instanceof BitfinexCandlestickSymbol) {
				sendCommand(new SubscribeCandlesCommand((BitfinexCandlestickSymbol) symbol));
			} else if(symbol instanceof BitfinexOrderBookSymbol) {
				sendCommand(new SubscribeOrderbookCommand((BitfinexOrderBookSymbol) symbol));
			} else {
				logger.error("Unknown stream symbol: {}", symbol);
			}
		}
		
		waitForChannelResubscription(oldChannelIdSymbolMap);
	}

	/**
	 * Wait for the successful channel re-subscription
	 * @param oldChannelIdSymbolMap
	 * @throws APIException
	 * @throws InterruptedException
	 */
	private void waitForChannelResubscription(final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap)
			throws APIException, InterruptedException {
		
		final Stopwatch stopwatch = Stopwatch.createStarted();
		final long MAX_WAIT_TIME_IN_MS = TimeUnit.MINUTES.toMillis(3);
		logger.info("Waiting for streams to resubscribe (max wait time {} msec)", MAX_WAIT_TIME_IN_MS);

		synchronized (channelIdToHandlerMap) {
			
			while(channelIdToHandlerMap.size() != oldChannelIdSymbolMap.size()) {
				
				if(stopwatch.elapsed(TimeUnit.MILLISECONDS) > MAX_WAIT_TIME_IN_MS) {
					handleResubscribeFailed(oldChannelIdSymbolMap);
				}
				
				channelIdToHandlerMap.wait(500);
			}
		}
	}

	/**
	 * Handle channel re-subscribe failed
	 * 
	 * @param oldChannelIdSymbolMap
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	private void handleResubscribeFailed(final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap)
			throws APIException, InterruptedException {
		
		final int requiredSymbols = oldChannelIdSymbolMap.size();
		final int subscribedSymbols = channelIdToHandlerMap.size();
		
		// Unsubscribe old channels before the symbol map is restored
		// otherwise we will get a lot of unknown symbol messages
		unsubscribeAllChannels();

		// Restore old symbol map for reconnect
		synchronized (channelIdToHandlerMap) {
			channelIdToHandlerMap.clear();
			channelIdToHandlerMap.putAll(oldChannelIdSymbolMap);
		}
		
		throw new APIException("Subscription of ticker failed: got only " 
				+ subscribedSymbols + " of " + requiredSymbols + " symbols subscribed");
	}

	/**
	 * Wait for unsubscription complete
	 *
	 * @throws InterruptedException
	 */
	public boolean unsubscribeAllChannels() throws InterruptedException {
		for(final ChannelCallbackHandler channel : channelIdToHandlerMap.values()) {
			BitfinexStreamSymbol symbol = channel.getSymbol();
			sendCommand(new UnsubscribeChannelCommand(symbol));
		}

		final Stopwatch stopwatch = Stopwatch.createStarted();

		synchronized (channelIdToHandlerMap) {
			while(! channelIdToHandlerMap.isEmpty()) {
				channelIdToHandlerMap.wait(500);

				// Wait max 1 minute for unsubscription complete
				if(stopwatch.elapsed(TimeUnit.SECONDS) >= 60) {
					logger.error("Unable to unsubscribe channels in 60 seconds");
					return false;
				}
			}
		}

		return true;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public BitfinexApiKeyPermissions getApiKeyPermissions() {
		return permissions;
	}

	public Collection<BitfinexStreamSymbol> getSubscribedChannels() {
		return channelIdToHandlerMap.values().stream()
				.map(ChannelCallbackHandler::getSymbol)
				.collect(Collectors.toList());
	}

	public BitfinexApiBrokerConfig getConfiguration() {
		return new BitfinexApiBrokerConfig(configuration);
	}

    public BitfinexApiCallbackListeners getCallbacks() {
        return callbackRegistry;
    }

	// managers getters

	public QuoteManager getQuoteManager() {
		return quoteManager;
	}

	public OrderbookManager getOrderbookManager() {
		return orderbookManager;
	}

	public RawOrderbookManager getRawOrderbookManager() {
		return rawOrderbookManager;
	}

	public PositionManager getPositionManager() {
		return positionManager;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public TradeManager getTradeManager() {
		return tradeManager;
	}

	public WalletManager getWalletManager() {
		return walletManager;
	}

	public ConnectionFeatureManager getConnectionFeatureManager() {
		return connectionFeatureManager;
	}
}
