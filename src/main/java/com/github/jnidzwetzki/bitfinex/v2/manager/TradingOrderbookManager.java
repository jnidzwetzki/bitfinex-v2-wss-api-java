package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradingOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradeOrderbookConfiguration;

public class TradingOrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<TradeOrderbookConfiguration, OrderbookEntry> channelCallbacks;

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
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService);
	}
	
	/**
	 * Register a new trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTradingOrderbookCallback(final TradeOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<TradeOrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTradingOrderbookCallback(final TradeOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<TradeOrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
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
		
		final int channel = bitfinexApiBroker.getChannelForSymbol(orderbookConfiguration);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + orderbookConfiguration);
		}
		
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(orderbookConfiguration);
	}
	
	/**
	 * Handle a new orderbook entry
	 * @param symbol
	 * @param tick
	 */
	public void handleNewOrderbookEntry(final TradeOrderbookConfiguration configuration, 
			final OrderbookEntry entry) {
		
		channelCallbacks.handleEvent(configuration, entry);
	}
}
