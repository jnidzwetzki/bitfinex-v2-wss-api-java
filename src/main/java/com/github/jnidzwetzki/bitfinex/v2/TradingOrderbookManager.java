package com.github.jnidzwetzki.bitfinex.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradingOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradeOrderbookConfiguration;

public class TradingOrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final Map<TradeOrderbookConfiguration, List<BiConsumer<TradeOrderbookConfiguration, OrderbookEntry>>> channelCallbacks;
	
	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The bitfinex API broker
	 */
	private final BitfinexApiBroker bitfinexApiBroker;

	public TradingOrderbookManager(final BitfinexApiBroker bitfinexApiBroker) {
		this.bitfinexApiBroker = bitfinexApiBroker;
		this.executorService = bitfinexApiBroker.getExecutorService();
		this.channelCallbacks = new HashMap<>();
	}
	
	/**
	 * Register a new trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTradingOrderbookCallback(final TradeOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<TradeOrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		channelCallbacks.putIfAbsent(orderbookConfiguration, new ArrayList<>());

		final List<BiConsumer<TradeOrderbookConfiguration, OrderbookEntry>> callbacks = channelCallbacks.get(orderbookConfiguration);
		
		synchronized (callbacks) {
			callbacks.add(callback);	
		}
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTradingOrderbookCallback(final TradeOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<String, OrderbookEntry> callback) throws APIException {
		
		if(! channelCallbacks.containsKey(orderbookConfiguration)) {
			throw new APIException("Unknown orderbook configuration: " + orderbookConfiguration);
		}
			
		final List<BiConsumer<TradeOrderbookConfiguration, OrderbookEntry>> callbacks = channelCallbacks.get(orderbookConfiguration);
		
		synchronized (callbacks) {
			return callbacks.remove(callback);
		}
	}
	
	/**
	 * Subscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void subscribeOrderbook(final TradeOrderbookConfiguration orderbookConfiguration) {
		
		final SubscribeTradingOrderbookCommand subscribeOrderbookCommand 
			= new SubscribeTradingOrderbookCommand(orderbookConfiguration);
		
		bitfinexApiBroker.sendCommand(subscribeOrderbookCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void unsubscribeOrderbook(final TradeOrderbookConfiguration orderbookConfiguration) {
		final String symbol = orderbookConfiguration.toJSON().toString();
		
		final int channel = bitfinexApiBroker.getChannelForSymbol(symbol);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + symbol);
		}
		
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(symbol);
	}
	
	/**
	 * Handle a new orderbook entry
	 * @param symbol
	 * @param tick
	 */
	public void handleNewOrderbookEntry(final TradeOrderbookConfiguration configuration, final OrderbookEntry entry) {
		
		final List<BiConsumer<TradeOrderbookConfiguration, OrderbookEntry>> callbacks 
			= channelCallbacks.get(configuration);
		
		if(callbacks == null) {
			return;
		}

		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}

			callbacks.forEach((c) -> {
				final Runnable runnable = () -> c.accept(configuration, entry);
				executorService.submit(runnable);
			});
		}
	}
}
