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
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;

public class OrderbookManager extends AbstractManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> channelCallbacks;

	public OrderbookManager(final BitfinexApiBroker bitfinexApiBroker, ExecutorService executorService) {
		super(bitfinexApiBroker, executorService);
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService, bitfinexApiBroker);
        bitfinexApiBroker.getCallbacks().onOrderbookEvent((sym,entries) -> {
			entries.forEach(e -> handleNewOrderbookEntry(sym, e));
		});
	}

	/**
	 * Register a new trading orderbook callback
	 * @throws APIException
	 */
	public void registerOrderbookCallback(final BitfinexOrderBookSymbol orderbookConfiguration,
			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws APIException {

		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}

	/**
	 * Remove the a trading orderbook callback
	 */
	public boolean removeOrderbookCallback(final BitfinexOrderBookSymbol orderbookConfiguration,
			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws APIException {

		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}

	/**
	 * Subscribe a orderbook
	 */
	public void subscribeOrderbook(final BitfinexOrderBookSymbol orderbookConfiguration) {

		final SubscribeOrderbookCommand subscribeOrderbookCommand
			= new SubscribeOrderbookCommand(orderbookConfiguration);

		bitfinexApiBroker.sendCommand(subscribeOrderbookCommand);
	}

	/**
	 * Unsubscribe a orderbook
	 */
	public void unsubscribeOrderbook(final BitfinexOrderBookSymbol orderbookConfiguration) {
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(orderbookConfiguration);
		bitfinexApiBroker.sendCommand(command);
	}

	/**
	 * Handle a new orderbook entry
	 */
	public void handleNewOrderbookEntry(final BitfinexOrderBookSymbol configuration,
			final BitfinexOrderBookEntry entry) {

		channelCallbacks.handleEvent(configuration, entry);
	}
}
