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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;

public class OrderbookManager extends AbstractManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<OrderbookConfiguration, OrderbookEntry> channelCallbacks;

	public OrderbookManager(final BitfinexApiBroker bitfinexApiBroker, ExecutorService executorService,
							BitfinexApiCallbackRegistry callbackRegistry) {
		super(bitfinexApiBroker, executorService);
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService, bitfinexApiBroker);
		callbackRegistry.onOrderbookEvent((sym,entries) -> {
			entries.forEach(e -> handleNewOrderbookEntry(sym, e));
		});
	}
	
	/**
	 * Register a new trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerOrderbookCallback(final OrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeOrderbookCallback(final OrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Subscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void subscribeOrderbook(final OrderbookConfiguration orderbookConfiguration) {
		
		final SubscribeOrderbookCommand subscribeOrderbookCommand 
			= new SubscribeOrderbookCommand(orderbookConfiguration);
		
		bitfinexApiBroker.sendCommand(subscribeOrderbookCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void unsubscribeOrderbook(final OrderbookConfiguration orderbookConfiguration) {
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(orderbookConfiguration);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(orderbookConfiguration);
	}
	
	/**
	 * Handle a new orderbook entry
	 * @param symbol
	 * @param tick
	 */
	public void handleNewOrderbookEntry(final OrderbookConfiguration configuration, 
			final OrderbookEntry entry) {
		
		channelCallbacks.handleEvent(configuration, entry);
	}
}
