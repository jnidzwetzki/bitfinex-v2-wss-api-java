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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Table;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.api.APICallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.DoNothingHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.HeartbeatHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.OrderHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.PositionHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.TradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.WalletHandler;
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
import com.github.jnidzwetzki.bitfinex.v2.commands.AbstractAPICommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CommandException;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeRawOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;

public class BitfinexApiBroker implements Closeable {

	/**
	 * The bitfinex api
	 */
	public final static String BITFINEX_URI = "wss://api.bitfinex.com/ws/2";
	
	/**
	 * The API callback
	 */
	private final Consumer<String> apiCallback = this::websocketCallback;
	
	/**
	 * The websocket endpoint
	 */
	private WebsocketClientEndpoint websocketEndpoint;
	
	/**
	 * The channel map
	 */
	private final Map<Integer, BitfinexStreamSymbol> channelIdSymbolMap;
	
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
	protected final AtomicLong lastHeatbeat;

	/**
	 * The heartbeat thread
	 */
	private Thread heartbeatThread;
	
	/**
	 * The API key
	 */
	private String apiKey;
	
	/**
	 * The API secret
	 */
	private String apiSecret;
	
	/**
	 * The dead man switch
	 */
	private boolean deadManSwitch = false;
	
	/**
	 * The connection ready latch
	 */
	private CountDownLatch connectionReadyLatch;
	private boolean ordersUpdated;
	private boolean walletsUpdated;
	private boolean positionsUpdated;
	
	/**
	 * Event on the latch until the connection is ready
	 * - Authenticated
	 * - Order snapshots read
	 * - Wallet snapshot read
	 * - Position snapshot read
	 */
	private final static int CONNECTION_READY_EVENTS = 4;
	
	/**
	 * The capabilities of the connection
	 */
	private ConnectionCapabilities capabilities = ConnectionCapabilities.NO_CAPABILITIES;
	
	/**
	 * Is the connection authenticated?
	 */
	private boolean authenticated;

	/**
	 * The channel handler
	 */
	private final Map<String, APICallbackHandler> channelHandler;
	
	/**
	 * The command callbacks
	 */
	private Map<String, CommandCallbackHandler> commandCallbacks;
	
	/**
	 * The executor service
	 */
	private final ExecutorService executorService;
	
	/**
	 * The sequence number auditor
	 */
	private final SequenceNumberAuditor sequenceNumberAuditor;
	
	/**
	 * The auth nonce producer
	 */
	public Supplier<String> authNonceProducer;

	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(BitfinexApiBroker.class);

	public BitfinexApiBroker(final String apiKey, final String apiSecret, final boolean deadManSwitch) {
		this(apiKey, apiSecret);
		this.deadManSwitch = deadManSwitch;
	}
	
	public BitfinexApiBroker(final String apiKey, final String apiSecret) {
		this();
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}
	
	public BitfinexApiBroker() {
		this.executorService = Executors.newFixedThreadPool(10);
		this.channelIdSymbolMap = new ConcurrentHashMap<>();
		this.lastHeatbeat = new AtomicLong();
		this.quoteManager = new QuoteManager(this, executorService);
		this.orderbookManager = new OrderbookManager(this, executorService);
		this.rawOrderbookManager = new RawOrderbookManager(this, executorService);
		this.orderManager = new OrderManager(this, executorService);
		this.tradeManager = new TradeManager(this, executorService);
		this.positionManager = new PositionManager(this, executorService);
		this.walletManager = new WalletManager(this, executorService);
		this.connectionFeatureManager = new ConnectionFeatureManager(this, executorService);
		this.capabilities = ConnectionCapabilities.NO_CAPABILITIES;
		this.channelHandler = new HashMap<>();
		this.sequenceNumberAuditor = new SequenceNumberAuditor();
		this.authNonceProducer = AuthCommand.AUTH_NONCE_PRODUCER_TIMESTAMP;
		
		setupChannelHandler();
		setupCommandCallbacks();
	}

	/**
	 * Setup the channel handler
	 */
	private void setupChannelHandler() {
		// Heartbeat
		final HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
		heartbeatHandler.onHeartbeatEvent(timestamp -> this.updateConnectionHeartbeat());
		channelHandler.put("hb", heartbeatHandler);

		final PositionHandler positionHandler = buildPositionHandler();
		
		// Position snapshot
		channelHandler.put("ps", positionHandler);
		// Position new
		channelHandler.put("pn", positionHandler);
		// Position updated
		channelHandler.put("pu", positionHandler);
		// Position canceled
		channelHandler.put("pc", positionHandler);

		// Founding offers
		channelHandler.put("fos", new DoNothingHandler());
		// Founding credits
		channelHandler.put("fcs", new DoNothingHandler());
		// Founding loans
		channelHandler.put("fls", new DoNothingHandler());
		// Ats - Unknown
		channelHandler.put("ats", new DoNothingHandler());

		final WalletHandler walletHandler = buildWalletHandler();
		// Wallet snapshot
		channelHandler.put("ws", walletHandler);
		// Wallet update
		channelHandler.put("wu", walletHandler);

		final OrderHandler orderHandler = buildOrderHandler();
		// Order snapshot
		channelHandler.put("os", orderHandler);
		// Order notification
		channelHandler.put("on", orderHandler);
		// Order update
		channelHandler.put("ou", orderHandler);
		// Order cancellation
		channelHandler.put("oc", orderHandler);

		final TradeHandler tradeHandler = buildTradeHandler();
		// Trade executed
		channelHandler.put("te", tradeHandler);
		// Trade updates
		channelHandler.put("tu", tradeHandler);

		final NotificationHandler notificationHandler = buildNotificationHandler();
		// General notification
		channelHandler.put("n", notificationHandler);
	}

	/**
	 * Build the notification handler
	 * 
	 * @return
	 */
	private NotificationHandler buildNotificationHandler() {
		
		final NotificationHandler notificationHandler = new NotificationHandler();
		
		notificationHandler.onExchangeOrderNotification(eo -> {
			eo.setApikey(getApiKey());
			orderManager.updateOrder(eo);
		});
		
		return notificationHandler;
	}

	/**
	 * Build the trade handler
	 * 
	 * @return
	 */
	private TradeHandler buildTradeHandler() {
		
		final TradeHandler tradeHandler = new TradeHandler();
		
		tradeHandler.onTradeEvent(trade -> {
			trade.setApikey(getApiKey());
			tradeManager.updateTrade(trade);
		});
		
		return tradeHandler;
	}

	/**
	 * Build the order handler 
	 * @return
	 */
	private OrderHandler buildOrderHandler() {
		final OrderHandler orderHandler = new OrderHandler();
		
		orderHandler.onExchangeOrdersEvent(exchangeOrders -> {
			for (ExchangeOrder exchangeOrder : exchangeOrders) {
				exchangeOrder.setApikey(getApiKey());
				orderManager.updateOrder(exchangeOrder);
			}
			if (!ordersUpdated) {
				ordersUpdated = true;
				connectionReadyLatch.countDown();
			}
		});
		
		return orderHandler;
	}

	/**
	 * Build the wallet handler
	 * @return
	 */
	private WalletHandler buildWalletHandler() {
		final WalletHandler walletHandler = new WalletHandler();
		
		walletHandler.onWalletsEvent(wallets -> {
			try {
				for (Wallet wallet : wallets) {
					Table<String, String, Wallet> walletTable = walletManager.getWalletTable();
					synchronized (walletTable) {
						walletTable.put(wallet.getWalletType(), wallet.getCurreny(), wallet);
						walletTable.notifyAll();
					}
				}
				if (!walletsUpdated) {
					walletsUpdated = true;
					connectionReadyLatch.countDown();
				}
			} catch (APIException e) {
				e.printStackTrace();
			}
		});
		
		return walletHandler;
	}

	/**
	 * Build the position handler 
	 * 
	 * @return
	 */
	private PositionHandler buildPositionHandler() {
		final PositionHandler positionHandler = new PositionHandler();
		
		positionHandler.onPositionsEvent(positions -> {
			for (Position position : positions) {
				positionManager.updatePosition(position);
			}
			if (!positionsUpdated) {
				positionsUpdated = true;
				connectionReadyLatch.countDown();
			}
		});
		
		return positionHandler;
	}
	
	/**
	 * Setup the command callbacks
	 */
	private void setupCommandCallbacks() {
		commandCallbacks = new HashMap<>();
		commandCallbacks.put("info", new DoNothingCommandCallback());

		final SubscribedCallback subscribed = new SubscribedCallback();
		subscribed.onSubscribedEvent((channelId, symbol) -> {
			synchronized (channelIdSymbolMap) {
				channelIdSymbolMap.put(channelId, symbol);
				channelIdSymbolMap.notifyAll();
			}
		});
		commandCallbacks.put("subscribed", subscribed);

		// TODO: hb is not ping:pong
		final ConnectionHeartbeatCallback pong = new ConnectionHeartbeatCallback();
		pong.onHeartbeatEvent(l -> this.updateConnectionHeartbeat());
		commandCallbacks.put("pong", pong);

		UnsubscribedCallback unsubscribed = new UnsubscribedCallback();
		unsubscribed.onUnsubscribedChannelEvent(channelId -> {
			synchronized (channelIdSymbolMap) {
				channelIdSymbolMap.remove(channelId);
				channelIdSymbolMap.notifyAll();
			}
		});
		commandCallbacks.put("unsubscribed", unsubscribed);

		final AuthCallback auth = new AuthCallback();
		auth.onAuthenticationSuccessEvent(c -> {
			this.capabilities = c;
			this.authenticated = true;
			this.connectionReadyLatch.countDown();
		});
		auth.onAuthenticationFailedEvent(c -> {
			this.capabilities = c;
			this.authenticated = false;
			while (connectionReadyLatch.getCount() != 0) {
				this.connectionReadyLatch.countDown();
			}
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
			connectionReadyLatch = new CountDownLatch(CONNECTION_READY_EVENTS);

			final URI bitfinexURI = new URI(BITFINEX_URI);
			websocketEndpoint = new WebsocketClientEndpoint(bitfinexURI);
			websocketEndpoint.addConsumer(apiCallback);
			websocketEndpoint.connect();
			updateConnectionHeartbeat();
			
			connectionFeatureManager.applyConnectionFeatures();
			authenticate();
			
			heartbeatThread = new Thread(new HeartbeatThread(this, websocketEndpoint));
			heartbeatThread.start();
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	/**
	 * Execute the authentication and wait until the socket is ready
	 * @throws InterruptedException
	 * @throws APIException 
	 */
	private void authenticate() throws InterruptedException, APIException {
        if (!canAuthenticate() || authenticated) {
            return;
        }
		sendCommand(new AuthCommand(authNonceProducer));
		logger.debug("Waiting for connection ready events");
		connectionReadyLatch.await(10, TimeUnit.SECONDS);

		if (!authenticated) {
			throw new APIException("Unable to perform authentication, capabilities are: " + capabilities);
		}
	}

	/**
	 * Is the connection to be authenticated
	 * @return
	 */
	private boolean canAuthenticate() {
		return apiKey != null && apiSecret != null;
	}

	/**
	 * Disconnect the websocket
	 */
	@Override
	public void close() {
		if(heartbeatThread != null) {
			heartbeatThread.interrupt();
			heartbeatThread = null;
		}
		
		if(websocketEndpoint != null) {
			websocketEndpoint.removeConsumer(apiCallback);
			websocketEndpoint.close();
			websocketEndpoint = null;
		}
		
		if(executorService != null) {
			executorService.shutdown();
		}
	}

	/**
	 * Send a new API command
	 * @param apiCommand
	 */
	public void sendCommand(final AbstractAPICommand apiCommand) {
		try {
			final String command = apiCommand.getCommand(this);
			logger.debug("Sending to server: {}", command);
			websocketEndpoint.sendMessage(command);
		} catch (CommandException e) {
			logger.error("Got Exception while sending command", e);
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

	/**
	 * Update the connection heartbeat
	 */
	public void updateConnectionHeartbeat() {
		lastHeatbeat.set(System.currentTimeMillis());
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
		
		if(channel == 0) {
			handleSignalingChannelData(message, jsonArray);
		} else {
			handleChannelData(jsonArray);
		}
	}

	/**
	 * Handle signaling channel data
	 * @param message
	 * @param jsonArray
	 */
	private void handleSignalingChannelData(final String message, final JSONArray jsonArray) {
		if (message.contains("ERROR")) {
			logger.error("Got Error message: {}", message);
		}
		final String subChannel = jsonArray.getString(1);
		APICallbackHandler apiCallbackHandler = channelHandler.get(subChannel);
		if (apiCallbackHandler == null) {
			logger.error("No match found for message {}", message);
			return;
		}
		try {
			apiCallbackHandler.handleChannelData(jsonArray);
		} catch (APIException e) {
			logger.error("Got exception while handling callback", e);
		}
	}

	/**
	 * Handle normal channel data
	 * @param jsonArray
	 * @throws APIException
	 */
	private void handleChannelData(final JSONArray jsonArray) {
		final int channel = jsonArray.getInt(0);
		final BitfinexStreamSymbol channelSymbol = channelIdSymbolMap.get(channel);
		if (channelSymbol == null) {
			logger.error("Unable to determine symbol for channel {} / data is {} ", channel, jsonArray);
			reconnect();
			return;
		}
		try {
			if (jsonArray.get(1) instanceof String) {
				handleChannelDataString(jsonArray, channelSymbol);
			} else {
				handleChannelDataArray(jsonArray, channelSymbol);
			}
		} catch (APIException e) {
			logger.error("Got exception while handling callback", e);
		}
	}

	/**
	 * Handle the channel data with has a string at first position
	 * @param jsonArray
	 * @param channelSymbol
	 * @throws APIException
	 */
	private void handleChannelDataString(final JSONArray jsonArray, 
			final BitfinexStreamSymbol channelSymbol) throws APIException {
		
		final String value = jsonArray.getString(1);
		
		if("hb".equals(value)) {
			quoteManager.updateChannelHeartbeat(channelSymbol);		
		} else if("te".equals(value)) {
			final JSONArray subarray = jsonArray.getJSONArray(2);			
			final ChannelCallbackHandler handler = new ExecutedTradeHandler();
			handler.handleChannelData(channelSymbol, subarray);
		} else if("tu".equals(value)) {
			// Ignore tu messages (see issue #13)
		} else {
			logger.error("Unable to process: {}", jsonArray);
		}
	}

	/**
	 * Handle the channel data with has an array at first position
	 * @param message
	 * @param channelSymbol
	 * @throws APIException
	 */
	private void handleChannelDataArray(final JSONArray message, final BitfinexStreamSymbol channelSymbol)
			throws APIException {
		final JSONArray jsonArray = message.getJSONArray(1);
		
		if(channelSymbol instanceof BitfinexCandlestickSymbol) {
			final CandlestickHandler handler = new CandlestickHandler();
			handler.onCandlesticksEvent(quoteManager::handleCandlestickCollection);
			handler.handleChannelData(channelSymbol, jsonArray);
		} else if(channelSymbol instanceof RawOrderbookConfiguration) {
			final RawOrderbookHandler handler = new RawOrderbookHandler();
			handler.onOrderbookEvent((sym, entries) -> {
				entries.forEach(e -> rawOrderbookManager.handleNewOrderbookEntry(sym, e));
			});
			handler.handleChannelData(channelSymbol, jsonArray);
		} else if(channelSymbol instanceof OrderbookConfiguration) {
			final OrderbookHandler handler = new OrderbookHandler();
			handler.onOrderbookEvent((sym,entries) -> {
				entries.forEach(e -> orderbookManager.handleNewOrderbookEntry(sym, e));
			});
			handler.handleChannelData(channelSymbol, jsonArray);
		} else if(channelSymbol instanceof BitfinexTickerSymbol) {
			final TickHandler handler = new TickHandler();
			handler.onTickEvent(quoteManager::handleNewTick);
			handler.handleChannelData(channelSymbol, jsonArray);
		} else if(channelSymbol instanceof BitfinexExecutedTradeSymbol) {
			final ExecutedTradeHandler handler = new ExecutedTradeHandler();
			handler.onExecutedTradeEvent((sym, trades) -> {
				trades.forEach(t -> quoteManager.handleExecutedTradeEntry(sym, t));
			});
			handler.handleChannelData(channelSymbol, jsonArray);
		} else {
			logger.error("Unknown stream type: {}", channelSymbol);
		}
	}

	/**
	 * Test whether the ticker is active or not 
	 * @param symbol
	 * @return
	 */
	public boolean isTickerActive(final BitfinexTickerSymbol symbol) {
		return getChannelForSymbol(symbol) != -1;
	}

	/**
	 * Find the channel for the given symbol
	 * @param symbol
	 * @return
	 */
	public int getChannelForSymbol(final BitfinexStreamSymbol symbol) {
		synchronized (channelIdSymbolMap) {
			return channelIdSymbolMap.entrySet()
					.stream()
					.filter((v) -> v.getValue().equals(symbol))
					.map(Map.Entry::getKey)
					.findAny().orElse(-1);
		}
	}
	
	/**
	 * Remove the channel for the given symbol
	 * @param symbol
	 * @return
	 */
	public boolean removeChannelForSymbol(final BitfinexStreamSymbol symbol) {
		final int channel = getChannelForSymbol(symbol);
		
		if(channel != -1) {
			synchronized (channelIdSymbolMap) {
				channelIdSymbolMap.remove(channel);
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Perform a reconnect
	 * @return
	 */
	public synchronized boolean reconnect() {
		try {
			logger.info("Performing reconnect");
			capabilities = ConnectionCapabilities.NO_CAPABILITIES;
			authenticated = false;
			sequenceNumberAuditor.reset();
			connectionFeatureManager.setActiveConnectionFeatures(0);
			
			// Invalidate old data
			quoteManager.invalidateTickerHeartbeat();
			orderManager.clear();
			positionManager.clear();
			
			websocketEndpoint.close();

			connectionReadyLatch = new CountDownLatch(CONNECTION_READY_EVENTS);
			ordersUpdated = false;
			walletsUpdated = false;
			positionsUpdated = false;
			websocketEndpoint.connect();
			
			connectionFeatureManager.applyConnectionFeatures();
			authenticate();
			resubscribeChannels();

			updateConnectionHeartbeat();
			
			return true;
		} catch (Exception e) {
			logger.error("Got exception while reconnect", e);
			websocketEndpoint.close();
			return false;
		}
	}

	/**
	 * Re-subscribe the old ticker
	 * @throws InterruptedException
	 * @throws APIException
	 */
	private void resubscribeChannels() throws InterruptedException, APIException {
		final Map<Integer, BitfinexStreamSymbol> oldChannelIdSymbolMap = new HashMap<>();

		synchronized (channelIdSymbolMap) {
			oldChannelIdSymbolMap.putAll(channelIdSymbolMap);
			channelIdSymbolMap.clear();
			channelIdSymbolMap.notifyAll();
		}
		
		// Resubscribe channels
		for(BitfinexStreamSymbol symbol : oldChannelIdSymbolMap.values()) {
			if(symbol instanceof BitfinexTickerSymbol) {
				sendCommand(new SubscribeTickerCommand((BitfinexTickerSymbol) symbol));
			} else if(symbol instanceof BitfinexExecutedTradeSymbol) {
				sendCommand(new SubscribeTradesCommand((BitfinexExecutedTradeSymbol) symbol));
			} else if(symbol instanceof BitfinexCandlestickSymbol) {
				sendCommand(new SubscribeCandlesCommand((BitfinexCandlestickSymbol) symbol));
			} else if(symbol instanceof OrderbookConfiguration) {
				sendCommand(new SubscribeOrderbookCommand((OrderbookConfiguration) symbol));
			} else if(symbol instanceof RawOrderbookConfiguration) {
				sendCommand(new SubscribeRawOrderbookCommand((RawOrderbookConfiguration) symbol));
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
	private void waitForChannelResubscription(final Map<Integer, BitfinexStreamSymbol> oldChannelIdSymbolMap)
			throws APIException, InterruptedException {
		
		final Stopwatch stopwatch = Stopwatch.createStarted();
		final long MAX_WAIT_TIME_IN_MS = TimeUnit.MINUTES.toMillis(3);
		logger.info("Waiting for streams to resubscribe (max wait time {} msec)", MAX_WAIT_TIME_IN_MS);

		synchronized (channelIdSymbolMap) {		
			
			while(channelIdSymbolMap.size() != oldChannelIdSymbolMap.size()) {
				
				if(stopwatch.elapsed(TimeUnit.MILLISECONDS) > MAX_WAIT_TIME_IN_MS) {
					handleResubscribeFailed(oldChannelIdSymbolMap);
				}
				
				channelIdSymbolMap.wait(500);
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
	private void handleResubscribeFailed(final Map<Integer, BitfinexStreamSymbol> oldChannelIdSymbolMap)
			throws APIException, InterruptedException {
		
		final int requiredSymbols = oldChannelIdSymbolMap.size();
		final int subscribedSymbols = channelIdSymbolMap.size();
		
		// Unsubscribe old channels before the symbol map is restored
		// otherwise we will get a lot of unknown symbol messages
		unsubscribeAllChannels();

		// Restore old symbol map for reconnect
		synchronized (channelIdSymbolMap) {
			channelIdSymbolMap.clear();
			channelIdSymbolMap.putAll(oldChannelIdSymbolMap);
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
		
		for(final Integer channel : channelIdSymbolMap.keySet()) {
			sendCommand(new UnsubscribeChannelCommand(channel));
		}

		final Stopwatch stopwatch = Stopwatch.createStarted();
		
		synchronized (channelIdSymbolMap) {
			while(! channelIdSymbolMap.isEmpty()) {
				channelIdSymbolMap.wait(500);
				
				// Wait max 1 minute for unsubscription complete
				if(stopwatch.elapsed(TimeUnit.SECONDS) >= 60) {
					logger.error("Unable to unsubscribe channels in 60 seconds");
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Get the last heartbeat value
	 * @return
	 */
	public AtomicLong getLastHeatbeat() {
		return lastHeatbeat;
	}
	
	/**
	 * Get the API key
	 * @return
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	/**
	 * Get the API secret
	 * @return
	 */
	public String getApiSecret() {
		return apiSecret;
	}
	
	/**
	 * Get the wallet manager
	 * @return
	 */
	public WalletManager getWalletManager() {
		return walletManager;
	}

	/**
	 * Get the ticker manager
	 * @return
	 */
	public QuoteManager getQuoteManager() {
		return quoteManager;
	}
	
	/**
	 * Get the order manager
	 * @return
	 */
	public OrderManager getOrderManager() {
		return orderManager;
	}
	
	/**
	 * Get the trade manager
	 * @return
	 */
	public TradeManager getTradeManager() {
		return tradeManager;
	}
	
	/**
	 * Get the orderbook manager
	 * @return
	 */
	public OrderbookManager getOrderbookManager() {
		return orderbookManager;
	}

	/**
	 * Get the raw orderbook manager
	 * @return
	 */
	public RawOrderbookManager getRawOrderbookManager() {
		return rawOrderbookManager;
	}
	
	/**
	 * Get the position manager
	 * @return
	 */
	public PositionManager getPositionManager() {
		return positionManager;
	}

	/**
	 * Get the connection capabilities
	 * @return
	 */
	public ConnectionCapabilities getCapabilities() {
		return capabilities;
	}
	
	/**
	 * Is the connection authenticated
	 * @return
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * Get the connection feature manager
	 * @return
	 */
	public ConnectionFeatureManager getConnectionFeatureManager() {
		return connectionFeatureManager;
	}
	
	/**
	 * Get the sequence number auditor
	 * @return
	 */
	public SequenceNumberAuditor getSequenceNumberAuditor() {
		return sequenceNumberAuditor;
	}
	
	/**
	 * Get the channel id symbol map
	 * @return
	 */
	public Map<Integer, BitfinexStreamSymbol> getChannelIdSymbolMap() {
		return channelIdSymbolMap;
	}
	
	/**
	 * Is the dead man feature enabled
	 * @return
	 */
	public boolean isDeadManFeatureEnabled() {
		return deadManSwitch;
	}

	/**
	 * Sets default auth nonce producer
	 * @param authNonceProducer 	- default nonce producer used for authentication
	 */
	public void setDefaultAuthNonceProducer(final Supplier<String> authNonceProducer) {
		this.authNonceProducer = Objects.requireNonNull(authNonceProducer);
	}
}
