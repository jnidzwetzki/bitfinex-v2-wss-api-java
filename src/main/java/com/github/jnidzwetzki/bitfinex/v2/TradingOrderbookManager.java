package com.github.jnidzwetzki.bitfinex.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradingOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookFrequency;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderBookPrecision;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradingOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;

public class TradingOrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final Map<TradingOrderbookConfiguration, List<BiConsumer<TradingOrderbookConfiguration, OrderbookEntry>>> channelCallbacks;
	
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
	public void registerTradingOrderbookCallback(final TradingOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<TradingOrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		channelCallbacks.putIfAbsent(orderbookConfiguration, new ArrayList<>());

		final List<BiConsumer<TradingOrderbookConfiguration, OrderbookEntry>> callbacks = channelCallbacks.get(orderbookConfiguration);
		
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
	public boolean removeTradingOrderbookCallback(final TradingOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<String, OrderbookEntry> callback) throws APIException {
		
		if(! channelCallbacks.containsKey(orderbookConfiguration)) {
			throw new APIException("Unknown orderbook configuration: " + orderbookConfiguration);
		}
			
		final List<BiConsumer<TradingOrderbookConfiguration, OrderbookEntry>> callbacks = channelCallbacks.get(orderbookConfiguration);
		
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
	public void subscribeOrderbook(final TradingOrderbookConfiguration orderbookConfiguration) {
		
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
	public void unsubscribeOrderbook(final TradingOrderbookConfiguration orderbookConfiguration) {
		
	}
}
