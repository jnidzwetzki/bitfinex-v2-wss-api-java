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
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
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
import com.google.common.base.Stopwatch;

public class SimpleBitfinexApiBroker implements Closeable, BitfinexWebsocketClient {

	/**
	 * The bitfinex api
	 */
	public final static String BITFINEX_URI = "wss://api.bitfinex.com/ws/2";

	/**
	 * The account info channel id
	 */
	public final static int ACCCOUNT_INFO_CHANNEL = 0;

	/**
	 * broker configuration
	 */
	private final BitfinexWebsocketConfiguration configuration;

	/**
	 * callback registry
	 */
	private final BitfinexApiCallbackRegistry callbackRegistry;

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

	/**
	 * Will not notify on connection state change
	 */
	private final boolean skipConnectionStateNotification;

	private final static Logger logger = LoggerFactory.getLogger(SimpleBitfinexApiBroker.class);

	public SimpleBitfinexApiBroker(final BitfinexWebsocketConfiguration config, final BitfinexApiCallbackRegistry callbackRegistry,
								   final SequenceNumberAuditor sequenceNumberAuditor, final boolean skipConnectionStateNotification) {
		this.configuration = new BitfinexWebsocketConfiguration(config);
		this.callbackRegistry = callbackRegistry;
		this.skipConnectionStateNotification = skipConnectionStateNotification;

		this.channelIdToHandlerMap = new ConcurrentHashMap<>();
		this.permissions = BitfinexApiKeyPermissions.NO_PERMISSIONS;
		this.sequenceNumberAuditor = sequenceNumberAuditor;
		this.lastHeartbeat = new AtomicLong(0);
		this.orderbookManager = new OrderbookManager(this, configuration.getExecutorService());
		this.rawOrderbookManager = new RawOrderbookManager(this, configuration.getExecutorService());
		this.orderManager = new OrderManager(this, configuration.getExecutorService());
		this.tradeManager = new TradeManager(this, configuration.getExecutorService());
		this.positionManager = new PositionManager(this, configuration.getExecutorService());
		this.walletManager = new WalletManager(this, configuration.getExecutorService());
        this.quoteManager = new QuoteManager(this, configuration.getExecutorService());
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
				final ChannelCallbackHandler channelCallbackHandler = createChannelCallbackHandler(channelId, symbol);
				channelIdToHandlerMap.put(channelId, channelCallbackHandler);
				channelIdToHandlerMap.notifyAll();
			}
			logger.debug("subscribed: {}", symbol);
			callbackRegistry.acceptSubscribeChannelEvent(symbol);
		});
		commandCallbacks.put("subscribed", subscribed);

		final UnsubscribedCallback unsubscribed = new UnsubscribedCallback();
		unsubscribed.onUnsubscribedChannelEvent(channelId -> {
			ChannelCallbackHandler removed;
			synchronized (channelIdToHandlerMap) {
				removed = channelIdToHandlerMap.remove(channelId);
				channelIdToHandlerMap.notifyAll();
			}
			if (removed != null) {
				callbackRegistry.acceptUnsubscribeChannelEvent(removed.getSymbol());
				logger.debug("unsubscribed: {}", removed.getSymbol());
			}

		});
		commandCallbacks.put("unsubscribed", unsubscribed);

		final AuthCallback auth = new AuthCallback();
		auth.onAuthenticationSuccessEvent(permissions -> {
		    logger.info("authentication succeeded for key {}", configuration.getApiKey());
			final BitfinexAccountSymbol symbol = BitfinexSymbols.account(permissions, configuration.getApiKey());
			final AccountInfoHandler handler = new AccountInfoHandler(ACCCOUNT_INFO_CHANNEL, symbol);
			handler.onHeartbeatEvent(timestamp -> this.updateConnectionHeartbeat());
			handler.onPositionsEvent(callbackRegistry::acceptMyPositionEvent);
			handler.onWalletsEvent(callbackRegistry::acceptMyWalletEvent);
			handler.onSubmittedOrderEvent(callbackRegistry::acceptMySubmittedOrderEvent);
			handler.onTradeEvent(callbackRegistry::acceptMyTradeEvent);
			handler.onOrderNotification(callbackRegistry::acceptMyOrderNotification);

			channelIdToHandlerMap.put(0, handler);
			callbackRegistry.acceptAuthenticationSuccessEvent(symbol);
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.AUTHENTICATION_SUCCESS);
		});
		auth.onAuthenticationFailedEvent(permissions -> {
            logger.info("authentication failed for key {}", configuration.getApiKey());
            final BitfinexAccountSymbol symbol = BitfinexSymbols.account(permissions, configuration.getApiKey());
			callbackRegistry.acceptAuthenticationFailedEvent(symbol);
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.AUTHENTICATION_FAILED);
		});
		commandCallbacks.put("auth", auth);

		final ConfCallback conf = new ConfCallback();
		conf.onConnectionFeatureEvent(connectionFeatureManager::setActiveConnectionFeatures);
		commandCallbacks.put("conf", conf);
		commandCallbacks.put("error", new ErrorCallback());
	}

	/**
	 * Open the connection
	 * @throws BitfinexClientException
	 */
	@Override
	public void connect() throws BitfinexClientException {
		logger.debug("connect() called");
		Closeable authSuccessEventCallback = null;
		Closeable authFailedCallback = null;
		Closeable positionInitCallback = null;
		Closeable walletsInitCallback = null;
		Closeable orderInitCallback = null;
		connectionStateChange(BitfinexConnectionStateEnum.CONNECTION_INIT);
		try {
            sequenceNumberAuditor.reset();
            final CountDownLatch connectionReadyLatch = new CountDownLatch(4);

            authSuccessEventCallback = callbackRegistry.onAuthenticationSuccessEvent(c -> {
                permissions = c.getPermissions();
                authenticated = true;
                connectionReadyLatch.countDown();
            });
            authFailedCallback = callbackRegistry.onAuthenticationFailedEvent(c -> {
                permissions = c.getPermissions();
                authenticated = false;
                while (connectionReadyLatch.getCount() != 0) {
                    connectionReadyLatch.countDown();
                }
            });
            positionInitCallback = callbackRegistry.onMyPositionEvent((a, p) -> connectionReadyLatch.countDown());
            walletsInitCallback = callbackRegistry.onMyWalletEvent((a, w) -> connectionReadyLatch.countDown());
            orderInitCallback = callbackRegistry.onMySubmittedOrderEvent((a, o) -> connectionReadyLatch.countDown());

            setupDefaultAccountInfoHandler();

			websocketEndpoint = new WebsocketClientEndpoint(new URI(BITFINEX_URI),
					this::websocketCallback,
					r -> callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE),
					t -> callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE)
			);
            websocketEndpoint.connect();
            updateConnectionHeartbeat();

            connectionFeatureManager.applyConnectionFeatures();
            if (configuration.isAuthenticationEnabled()) {
                authenticateAndWait(connectionReadyLatch);
            }

            if (configuration.isHeartbeatThreadActive()) {
                heartbeatThread = new Thread(new HeartbeatThread(this, websocketEndpoint, lastHeartbeat::get));
                heartbeatThread.start();
            }
			connectionStateChange(BitfinexConnectionStateEnum.CONNECTION_SUCCESS);
		} catch (final Exception e) {
			connectionStateChange(BitfinexConnectionStateEnum.CONNECTION_FAILED);
			throw new BitfinexClientException(e);
		} finally {
			Optional.ofNullable(authSuccessEventCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(authFailedCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(positionInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(walletsInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(orderInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
		}
	}

	/**
	 * Setup the default info handler - can be replaced in onAuthenticationSuccessEvent
	 */
	private void setupDefaultAccountInfoHandler() {
		final BitfinexAccountSymbol accountSymbol = BitfinexSymbols.account(BitfinexApiKeyPermissions.NO_PERMISSIONS);
		final AccountInfoHandler accountInfoHandler = new AccountInfoHandler(ACCCOUNT_INFO_CHANNEL, accountSymbol);
		accountInfoHandler.onHeartbeatEvent(timestamp -> this.updateConnectionHeartbeat());
		channelIdToHandlerMap.put(ACCCOUNT_INFO_CHANNEL, accountInfoHandler);
	}

	/**
	 * Disconnect the websocket
	 */
	@Override
	public void close() {
		try {
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_INIT);
			logger.debug("close() called");
			if (heartbeatThread != null) {
				heartbeatThread.interrupt();
				heartbeatThread = null;
			}

			if (websocketEndpoint != null) {
				websocketEndpoint.close();
				websocketEndpoint = null;
			}
			connectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_SUCCESS);
		} catch (final Exception e) {
			connectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_FAILED);
			throw new BitfinexClientException(e);
		}
	}

	/**
	 * Send a new API command
	 * @param command
	 */
	@Override
	public void sendCommand(final BitfinexCommand command) {
		try {
			if (command instanceof BitfinexStreamSymbolToChannelIdResolverAware) {
				final BitfinexStreamSymbolToChannelIdResolverAware aware = (BitfinexStreamSymbolToChannelIdResolverAware) command;
				aware.setResolver(symbol -> {
					final Integer channelId = getChannelForSymbol(symbol);
					if (channelId == null) {
						throw new IllegalArgumentException("Unknown symbol: " + symbol);
					}
					return channelId;
				});
			}
			final String json = command.getCommand(this);
            logger.debug("Sent: {}", command);
			websocketEndpoint.sendMessage(json);
		} catch (final BitfinexCommandException e) {
			logger.error("Got Exception while sending command", e);
		}
	}

	/**
	 * Perform a reconnect
	 * @return true if reconnect succeeded, false otherwise
	 */
	@Override
	public synchronized boolean reconnect() {
		logger.debug("reconnect() called");
		Closeable authSuccessEventCallback = null;
		Closeable authFailedCallback = null;
		Closeable positionInitCallback = null;
		Closeable walletsInitCallback = null;
		Closeable orderInitCallback = null;
		try {
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.RECONNECTION_INIT);
			websocketEndpoint.close();

			permissions = BitfinexApiKeyPermissions.NO_PERMISSIONS;
			authenticated = false;
			sequenceNumberAuditor.reset();
			connectionFeatureManager.setActiveConnectionFeatures(0);

			// Invalidate old data
			quoteManager.invalidateTickerHeartbeat();
			orderManager.clear();
			positionManager.clear();

			final CountDownLatch connectionReadyLatch = new CountDownLatch(4);

			authSuccessEventCallback = callbackRegistry.onAuthenticationSuccessEvent(c -> {
				permissions = c.getPermissions();
				authenticated = true;
				connectionReadyLatch.countDown();
			});
			authFailedCallback = callbackRegistry.onAuthenticationFailedEvent(c -> {
				permissions = c.getPermissions();
				authenticated = false;
				while (connectionReadyLatch.getCount() != 0) {
					connectionReadyLatch.countDown();
				}
			});

			positionInitCallback = callbackRegistry.onMyPositionEvent((a, p) -> connectionReadyLatch.countDown());
			walletsInitCallback = callbackRegistry.onMyWalletEvent((a, w) -> connectionReadyLatch.countDown());
			orderInitCallback = callbackRegistry.onMySubmittedOrderEvent((a, o) -> connectionReadyLatch.countDown());

			// Reset account info handler
			setupDefaultAccountInfoHandler();

			websocketEndpoint.connect();

			connectionFeatureManager.applyConnectionFeatures();
			if( configuration.isAuthenticationEnabled()) {
				authenticateAndWait(connectionReadyLatch);
			}
			resubscribeChannels();

			updateConnectionHeartbeat();
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.RECONNECTION_SUCCESS);
			return true;
		} catch (final Exception e) {
			logger.error("Got exception while reconnect", e);
			websocketEndpoint.close();
			callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.RECONNECTION_FAILED);
			return false;
		} finally {
			Optional.ofNullable(authSuccessEventCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(authFailedCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(positionInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(walletsInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
			Optional.ofNullable(orderInitCallback).ifPresent(SimpleBitfinexApiBroker::safeClose);
		}
	}

	private ChannelCallbackHandler createChannelCallbackHandler(final int channelId, final BitfinexStreamSymbol symbol) {
		if (symbol instanceof BitfinexCandlestickSymbol) {
			final CandlestickHandler handler = new CandlestickHandler(channelId, (BitfinexCandlestickSymbol) symbol);
			handler.onCandlesticksEvent(callbackRegistry::acceptCandlesticksEvent);
			return handler;
		} else if (symbol instanceof BitfinexOrderBookSymbol) {
			final BitfinexOrderBookSymbol orderBookSymbol = (BitfinexOrderBookSymbol) symbol;
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
	 * @throws BitfinexClientException
	 */
	private void authenticateAndWait(final CountDownLatch latch) throws InterruptedException, BitfinexClientException {
		if (authenticated) {
			return;
		}
		sendCommand(new AuthCommand(configuration.getAuthNonceProducer()));
		logger.debug("Waiting for connection ready events");
		latch.await(10, TimeUnit.SECONDS);

		if (!authenticated) {
			throw new BitfinexClientException("Unable to perform authentication, permissions are: " + permissions);
		}
	}

	/**
	 * We received a websocket callback
	 * @param message
	 */
	private void websocketCallback(final String message) {
		logger.debug("Recv: {}", message);
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
		// JSON callback
        final JSONObject jsonObject = new JSONObject(message);
		final String eventType = jsonObject.getString("event");

		final CommandCallbackHandler commandCallbackHandler = commandCallbacks.get(eventType);
		if( commandCallbackHandler == null ) {
			logger.error("Unknown event: {}", message);
			return;
		}
		try {
			commandCallbackHandler.handleChannelData(jsonObject);
		} catch (final BitfinexClientException e) {
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
		updateConnectionHeartbeat();

		// JSON callback
		final JSONArray jsonArray = new JSONArray(new JSONTokener(message));

		if(connectionFeatureManager.isConnectionFeatureActive(BitfinexConnectionFeature.SEQ_ALL)) {
			sequenceNumberAuditor.auditPackage(jsonArray);
		}

		final int channel = jsonArray.getInt(0);
		final ChannelCallbackHandler channelCallbackHandler = channelIdToHandlerMap.get(channel);
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
		if (Objects.equals(action, "hb")) {
			quoteManager.updateChannelHeartbeat(channelCallbackHandler.getSymbol());
		}
		try {
			if (payload == null) {
				return;
			}
			channelCallbackHandler.handleChannelData(action, payload);
		} catch (final BitfinexClientException e) {
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
	 * @return
	 * @throws InterruptedException
	 * @throws BitfinexClientException
	 */
	private void resubscribeChannels() throws InterruptedException, BitfinexClientException {
		final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap = new HashMap<>();

		synchronized (channelIdToHandlerMap) {
			oldChannelIdSymbolMap.putAll(channelIdToHandlerMap);
			channelIdToHandlerMap.clear();
			channelIdToHandlerMap.notifyAll();

			// Implicit channel account info channel
			channelIdToHandlerMap.put(ACCCOUNT_INFO_CHANNEL, oldChannelIdSymbolMap.get(ACCCOUNT_INFO_CHANNEL));
		}

		// Resubscribe channels
		for(final ChannelCallbackHandler handler : oldChannelIdSymbolMap.values()) {
			final BitfinexStreamSymbol symbol = handler.getSymbol();
			if(symbol instanceof BitfinexTickerSymbol) {
				sendCommand(new SubscribeTickerCommand((BitfinexTickerSymbol) symbol));
			} else if(symbol instanceof BitfinexExecutedTradeSymbol) {
				sendCommand(new SubscribeTradesCommand((BitfinexExecutedTradeSymbol) symbol));
			} else if(symbol instanceof BitfinexCandlestickSymbol) {
				sendCommand(new SubscribeCandlesCommand((BitfinexCandlestickSymbol) symbol));
			} else if(symbol instanceof BitfinexOrderBookSymbol) {
				sendCommand(new SubscribeOrderbookCommand((BitfinexOrderBookSymbol) symbol));
			} else if(symbol instanceof BitfinexAccountSymbol) {
				// This symbol does not need to be resubscribed
			} else {
				logger.error("Unknown stream symbol: {}", symbol);
			}
		}

		waitForChannelResubscription(oldChannelIdSymbolMap);
	}

	/**
	 * Wait for the successful channel re-subscription
	 * @param oldChannelIdSymbolMap
	 * @return
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 */
	private void waitForChannelResubscription(final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap)
			throws BitfinexClientException, InterruptedException {

		final Stopwatch stopwatch = Stopwatch.createStarted();
		final long MAX_WAIT_TIME_IN_MS = TimeUnit.MINUTES.toMillis(3);
		logger.info("Waiting for streams to resubscribe (max wait time {} msec)", MAX_WAIT_TIME_IN_MS);

		synchronized (channelIdToHandlerMap) {

			while(channelIdToHandlerMap.size() != oldChannelIdSymbolMap.size()) {

				if(stopwatch.elapsed(TimeUnit.MILLISECONDS) > MAX_WAIT_TIME_IN_MS) {
					// Raise BitfinexClientException
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
	 * @throws BitfinexClientException
	 * @throws InterruptedException
	 */
	private void handleResubscribeFailed(final Map<Integer, ChannelCallbackHandler> oldChannelIdSymbolMap)
			throws BitfinexClientException, InterruptedException {

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

		throw new BitfinexClientException("Subscription of ticker failed: got only "
				+ subscribedSymbols + " of " + requiredSymbols + " symbols subscribed");
	}

	/**
	 * Wait for unsubscription complete
	 */
	@Override
	public boolean unsubscribeAllChannels() {
        final Collection<BitfinexStreamSymbol> channels = getSubscribedChannels();
        final int channelsToUnsubscribe = channels.size();

        logger.debug("Calling unsubscribe for {} channels", channelsToUnsubscribe);
		final CountDownLatch countDownLatch = new CountDownLatch(channelsToUnsubscribe);

        try (Closeable c = callbackRegistry.onUnsubscribeChannelEvent(s -> countDownLatch.countDown())) {
            channels.forEach(symbol -> sendCommand(new UnsubscribeChannelCommand(symbol)));

            // Await the unsubscription
            countDownLatch.await(30, TimeUnit.SECONDS);

            return true;
        } catch (final InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            return false;
        }
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public BitfinexApiKeyPermissions getApiKeyPermissions() {
		return permissions;
	}

	@Override
	public Collection<BitfinexStreamSymbol> getSubscribedChannels() {
		return channelIdToHandlerMap.values().stream()
				.map(ChannelCallbackHandler::getSymbol)
				.filter(s -> !(s instanceof BitfinexAccountSymbol))
				.collect(Collectors.toList());
	}

	@Override
	public BitfinexWebsocketConfiguration getConfiguration() {
		return new BitfinexWebsocketConfiguration(configuration);
	}

    @Override
	public BitfinexApiCallbackListeners getCallbacks() {
        return callbackRegistry;
    }

	private void connectionStateChange(BitfinexConnectionStateEnum state) {
		if (!skipConnectionStateNotification) {
			callbackRegistry.acceptConnectionStateChange(state);
		}
	}

	private static void safeClose(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ignored) {

		}
	}

	// managers getters

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
}
