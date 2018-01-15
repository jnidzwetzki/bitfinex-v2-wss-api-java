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
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeRawOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookEntry;

public class RawOrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<RawOrderbookConfiguration, RawOrderbookEntry> channelCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The bitfinex API broker
	 */
	private final BitfinexApiBroker bitfinexApiBroker;

	public RawOrderbookManager(final BitfinexApiBroker bitfinexApiBroker) {
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
	public void registerOrderbookCallback(final RawOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback) throws APIException {
		
		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeOrderbookCallback(final RawOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback) throws APIException {
		
		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Subscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void subscribeOrderbook(final RawOrderbookConfiguration orderbookConfiguration) {
		
		final SubscribeRawOrderbookCommand subscribeOrderbookCommand 
			= new SubscribeRawOrderbookCommand(orderbookConfiguration);
		
		bitfinexApiBroker.sendCommand(subscribeOrderbookCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void unsubscribeOrderbook(final RawOrderbookConfiguration orderbookConfiguration) {
		
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
	public void handleNewOrderbookEntry(final RawOrderbookConfiguration configuration, 
			final RawOrderbookEntry entry) {
		
		channelCallbacks.handleEvent(configuration, entry);
	}
}
