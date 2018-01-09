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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
import com.github.jnidzwetzki.bitfinex.v2.callback.api.WalletHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.TickHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.TradeOrderbookHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.AuthCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.CommandCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ConnectionHeartbeatCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.DoNothingCommandCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.SubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.UnsubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.commands.AbstractAPICommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CommandException;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradingOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class BitfinexApiBroker implements Closeable {

	/**
	 * The bitfinex api
	 */
	public final static String BITFINEX_URI = "wss://api.bitfinex.com/ws/2";
	
	/**
	 * The API callback
	 */
	private final Consumer<String> apiCallback = ((c) -> websocketCallback(c));
	
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
	 * The position manager
	 */
	private final PositionManager positionManager;
	
	/**
	 * The order manager
	 */
	private final OrderManager orderManager;
	
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
	 * The connection ready latch
	 */
	private CountDownLatch connectionReadyLatch;
	
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
	 * Wallets
	 * 
	 *  Currency, Wallet-Type, Wallet
	 */
	private final Table<String, String, Wallet> walletTable;
	
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
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(BitfinexApiBroker.class);


	public BitfinexApiBroker(final String apiKey, final String apiSecret) {
		this();
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}
	
	public BitfinexApiBroker() {
		this.executorService = Executors.newFixedThreadPool(10);
		this.channelIdSymbolMap = new HashMap<>();
		this.lastHeatbeat = new AtomicLong();
		this.quoteManager = new QuoteManager(this);
		this.orderbookManager = new OrderbookManager(this);
		this.orderManager = new OrderManager(this);
		this.positionManager = new PositionManager(executorService);
		this.walletTable = HashBasedTable.create();
		this.capabilities = ConnectionCapabilities.NO_CAPABILITIES;
		this.authenticated = false;
		this.channelHandler = new HashMap<>();

		setupChannelHandler();
		setupCommandCallbacks();
	}

	/**
	 * Setup the channel handler
	 */
	private void setupChannelHandler() {
		// Heartbeat
		channelHandler.put("hb", new HeartbeatHandler());
		// Position snapshot
		channelHandler.put("ps", new PositionHandler());
		// Position new
		channelHandler.put("pn", new PositionHandler());
		// Position updated
		channelHandler.put("pu", new PositionHandler());
		// Position caneled
		channelHandler.put("pc", new PositionHandler());
		// Founding offers
		channelHandler.put("fos", new DoNothingHandler());
		// Founding credits
		channelHandler.put("fcs", new DoNothingHandler());
		// Founding loans
		channelHandler.put("fls", new DoNothingHandler());
		// Ats - Unkown
		channelHandler.put("ats", new DoNothingHandler());
		// Wallet snapshot
		channelHandler.put("ws", new WalletHandler());
		// Wallet update
		channelHandler.put("wu", new WalletHandler());
		// Order snapshot
		channelHandler.put("os", new OrderHandler());
		// Order notification
		channelHandler.put("on", new OrderHandler());
		// Order update
		channelHandler.put("ou", new OrderHandler());
		// Order cancelation
		channelHandler.put("oc", new OrderHandler());
		// Trade executed
		channelHandler.put("te", new DoNothingHandler());
		// Trade update
		channelHandler.put("tu", new DoNothingHandler());
		// General notification 
		channelHandler.put("n", new NotificationHandler());
	}
	
	/**
	 * Setup the command callbacks
	 */
	private void setupCommandCallbacks() {
		commandCallbacks = new HashMap<>();
		commandCallbacks.put("info", new DoNothingCommandCallback());
		commandCallbacks.put("subscribed", new SubscribedCallback());
		commandCallbacks.put("pong", new ConnectionHeartbeatCallback());
		commandCallbacks.put("unsubscribed", new UnsubscribedCallback());
		commandCallbacks.put("auth", new AuthCallbackHandler());
	}
	
	/**
	 * Open the connection
	 * @throws APIException
	 */
	public void connect() throws APIException {
		try {
			final URI bitfinexURI = new URI(BITFINEX_URI);
			websocketEndpoint = new WebsocketClientEndpoint(bitfinexURI);
			websocketEndpoint.addConsumer(apiCallback);
			websocketEndpoint.connect();
			updateConnectionHeartbeat();
			
			executeAuthentification();
			
			heartbeatThread = new Thread(new HeartbeatThread(this));
			heartbeatThread.start();
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	/**
	 * Execute the authentification and wait until the socket is ready
	 * @throws InterruptedException
	 * @throws APIException 
	 */
	private void executeAuthentification() throws InterruptedException, APIException {
		connectionReadyLatch = new CountDownLatch(CONNECTION_READY_EVENTS);

		if(isAuthenticatedConnection()) {
			sendCommand(new AuthCommand());
			logger.info("Waiting for connection ready events");
			connectionReadyLatch.await(10, TimeUnit.SECONDS);
			
			if(! authenticated) {
				throw new APIException("Unable to perform authentification, capabilities are: " + capabilities);
			}
		}
	}

	/**
	 * Is the connection to be authenticated
	 * @return
	 */
	private boolean isAuthenticatedConnection() {
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
	 * Get the websocket endpoint
	 * @return
	 */
	public WebsocketClientEndpoint getWebsocketEndpoint() {
		return websocketEndpoint;
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
		final JSONTokener tokener = new JSONTokener(message);
		final JSONObject jsonObject = new JSONObject(tokener);
		
		final String eventType = jsonObject.getString("event");
		
		if(! commandCallbacks.containsKey(eventType)) {
			logger.error("Unknown event: {}", message);
		} else {
			try {
				final CommandCallbackHandler callback = commandCallbacks.get(eventType);
				callback.handleChannelData(this, jsonObject);
			} catch (APIException e) {
				logger.error("Got an exception while handling callback");
			}
		}
	}

	/**
	 * Remove the channel
	 * @param channelId
	 */
	public void removeChannel(final int channelId) {
		synchronized (channelIdSymbolMap) {
			channelIdSymbolMap.remove(channelId);
			channelIdSymbolMap.notifyAll();
		}
	}

	/**
	 * Update the connection heartbeat
	 */
	public void updateConnectionHeartbeat() {
		lastHeatbeat.set(System.currentTimeMillis());
	}

	/**
	 * Add channel to symbol map
	 * @param channelId
	 * @param symbol
	 */
	public void addToChannelSymbolMap(final int channelId, final BitfinexStreamSymbol symbol) {
		synchronized (channelIdSymbolMap) {
			channelIdSymbolMap.put(channelId, symbol);
			channelIdSymbolMap.notifyAll();
		}
	}

	/**
	 * Handle a channel callback
	 * @param message
	 */
	protected void handleChannelCallback(final String message) {
		// Channel callback
		logger.debug("Channel callback");
		updateConnectionHeartbeat();

		// JSON callback
		final JSONTokener tokener = new JSONTokener(message);
		final JSONArray jsonArray = new JSONArray(tokener);
				
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
		
		if(message.contains("ERROR")) {
			logger.error("Got Error message: {}", message);
		}
		
		final String subchannel = jsonArray.getString(1);

		if(! channelHandler.containsKey(subchannel)) {
			logger.error("No match found for message {}", message);
		} else {
			final APICallbackHandler channelHandlerCallback = channelHandler.get(subchannel);
			
			try {
				channelHandlerCallback.handleChannelData(this, jsonArray);
			} catch (APIException e) {
				logger.error("Got exception while handling callback", e);
			}
		}
	}

	/**
	 * Handle normal channel data
	 * @param jsonArray
	 * @param channel
	 * @throws APIException 
	 */
	private void handleChannelData(final JSONArray jsonArray) {
		final int channel = jsonArray.getInt(0);
		final BitfinexStreamSymbol channelSymbol = getFromChannelSymbolMap(channel);

		if(channelSymbol == null) {
			logger.error("Unable to determine symbol for channel {}", channel);
			logger.error("Data is {}", jsonArray);
			return;
		}
		
		if(jsonArray.get(1) instanceof String) {
			final String value = jsonArray.getString(1);
			
			if("hb".equals(value)) {
				quoteManager.updateChannelHeartbeat(channelSymbol);		
			} else {
				logger.error("Unable to process: {}", jsonArray);
			}
		} else {	
			final JSONArray subarray = jsonArray.getJSONArray(1);			

			try {
				if(channelSymbol instanceof BitfinexCandlestickSymbol) {
					final ChannelCallbackHandler handler = new CandlestickHandler();
					handler.handleChannelData(this, channelSymbol, subarray);
				} else if(channelSymbol instanceof OrderbookConfiguration) {
					final TradeOrderbookHandler handler = new TradeOrderbookHandler();
					handler.handleChannelData(this, channelSymbol, subarray);
				} else if(channelSymbol instanceof BitfinexCurrencyPair) {
					final ChannelCallbackHandler handler = new TickHandler();
					handler.handleChannelData(this, channelSymbol, subarray);
				} else {
					logger.error("Unknown stream type: {}", channelSymbol);
				}
			} catch (APIException e) {
				logger.error("Got exception while handling callback", e);
			}
		}
	}

	/**
	 * Get the channel from the symbol map - thread safe
	 * @param channel
	 * @return
	 */
	public BitfinexStreamSymbol getFromChannelSymbolMap(final int channel) {
		synchronized (channelIdSymbolMap) {
			return channelIdSymbolMap.get(channel);
		}
	}
	
	/**
	 * Test whether the ticker is active or not 
	 * @param symbol
	 * @return
	 */
	public boolean isTickerActive(final BitfinexCurrencyPair symbol) {
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
					.map((v) -> v.getKey())
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
			
			// Invalidate old data
			quoteManager.invalidateTickerHeartbeat();
			orderManager.clear();
			positionManager.clear();
			
			websocketEndpoint.close();
			websocketEndpoint.connect();
			
			executeAuthentification();
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
	 * Resubscribe the old ticker
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
			if(symbol instanceof BitfinexCurrencyPair) {
				sendCommand(new SubscribeTickerCommand((BitfinexCurrencyPair) symbol));
			} else if(symbol instanceof BitfinexCandlestickSymbol) {
				sendCommand(new SubscribeCandlesCommand((BitfinexCandlestickSymbol) symbol));
			} else if(symbol instanceof OrderbookConfiguration) {
				sendCommand(new SubscribeTradingOrderbookCommand((OrderbookConfiguration) symbol));
			} else {
				logger.error("Unknown stream symbol: {}", symbol);
			}
		}
		
		logger.info("Waiting for streams to resubscribe");
		int execution = 0;
		
		synchronized (channelIdSymbolMap) {		
			while(channelIdSymbolMap.size() != oldChannelIdSymbolMap.size()) {
				
				if(execution > 10) {
					
					// Restore old map for reconnect
					synchronized (channelIdSymbolMap) {
						channelIdSymbolMap.clear();
						channelIdSymbolMap.putAll(oldChannelIdSymbolMap);
					}
					
					throw new APIException("Subscription of ticker failed");
				}
				
				channelIdSymbolMap.wait(500);
				execution++;	
			}
		}
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
	 * Get all wallets
	 * @return 
	 * @throws APIException 
	 */
	public Collection<Wallet> getWallets() throws APIException {		
		
		throwExceptionIfUnauthenticated();
		
		synchronized (walletTable) {
			return Collections.unmodifiableCollection(walletTable.values());
		}
	}
	
	/**
	 * Get all wallets
	 * @return 
	 * @throws APIException 
	 */
	public Table<String, String, Wallet> getWalletTable() throws APIException {
		return walletTable;
	}

	/**
	 * Throw a new exception if called on a unauthenticated connection
	 * @throws APIException
	 */
	private void throwExceptionIfUnauthenticated() throws APIException {
		if(! authenticated) {
			throw new APIException("Unable to perform operation on an unauthenticated connection");
		}
	}

	/**
	 * Get the ticker manager
	 * @return
	 */
	public QuoteManager getQuoteManager() {
		return quoteManager;
	}
	
	/**
	 * Get the connection ready latch
	 * @return
	 */
	public CountDownLatch getConnectionReadyLatch() {
		return connectionReadyLatch;
	}

	/**
	 * Get the executor service
	 * @return
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	/**
	 * Get the order manager
	 * @return
	 */
	public OrderManager getOrderManager() {
		return orderManager;
	}
	
	/**
	 * Get the orderbook manager
	 * @return
	 */
	public OrderbookManager getOrderbookManager() {
		return orderbookManager;
	}

	/**
	 * Get the position manager
	 * @return
	 */
	public PositionManager getPositionManager() {
		return positionManager;
	}
	
	/**
	 * Set new connection capabilities
	 * @param capabilities
	 */
	public void setCapabilities(final ConnectionCapabilities capabilities) {
		this.capabilities = capabilities;
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
	 * Set connection auth status
	 * @param authenticated
	 */
	public void setAuthenticated(final boolean authenticated) {
		this.authenticated = authenticated;
	}
}
