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

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;

public class OrderbookManager extends AbstractManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> channelCallbacks;

	public OrderbookManager(final BitfinexWebsocketClient client, ExecutorService executorService) {
		super(client, executorService);
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService, client);
        client.getCallbacks().onOrderbookEvent((sym,entries) -> {
			entries.forEach(e -> handleNewOrderbookEntry(sym, e));
		});
	}

	/**
	 * Register a new trading orderbook callback
	 * @throws BitfinexClientException
	 */
	public void registerOrderbookCallback(final BitfinexOrderBookSymbol orderbookConfiguration,
			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws BitfinexClientException {

		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}

	/**
	 * Remove the a trading orderbook callback
	 */
	public boolean removeOrderbookCallback(final BitfinexOrderBookSymbol orderbookConfiguration,
			final BiConsumer<BitfinexOrderBookSymbol, BitfinexOrderBookEntry> callback) throws BitfinexClientException {

		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}

	/**
	 * Subscribe a orderbook
	 */
	public void subscribeOrderbook(final BitfinexOrderBookSymbol orderbookConfiguration) {

		final SubscribeOrderbookCommand subscribeOrderbookCommand
			= new SubscribeOrderbookCommand(orderbookConfiguration);

		client.sendCommand(subscribeOrderbookCommand);
	}

	/**
	 * Unsubscribe a orderbook
	 */
	public void unsubscribeOrderbook(final BitfinexOrderBookSymbol orderbookConfiguration) {
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(orderbookConfiguration);
		client.sendCommand(command);
	}

	/**
	 * Handle a new orderbook entry
	 */
	public void handleNewOrderbookEntry(final BitfinexOrderBookSymbol configuration,
			final BitfinexOrderBookEntry entry) {

		channelCallbacks.handleEvent(configuration, entry);
	}
}
