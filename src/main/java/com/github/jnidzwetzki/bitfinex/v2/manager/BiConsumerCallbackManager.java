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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;

public class BiConsumerCallbackManager<S, T> extends AbstractManager {

	/**
	 * The callbacks
	 */
	private final Map<S, List<BiConsumer<S, T>>> callbacks;
	
	public BiConsumerCallbackManager(final ExecutorService executorService, 
			final BitfinexWebsocketClient client) {
		
		super(client, executorService);
		this.callbacks = new ConcurrentHashMap<>();
	}
	
	/**
	 * Register a new callback
	 * @param symbol
	 * @param callback
	 * @throws BitfinexClientException
	 */
	public void registerCallback(final S symbol, final BiConsumer<S, T> callback) throws BitfinexClientException {
		final List<BiConsumer<S, T>> callbackList 
			= callbacks.computeIfAbsent(symbol, (k) -> new CopyOnWriteArrayList<>());	
		callbackList.add(callback);
	}
	
	/**
	 * Remove the a callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws BitfinexClientException
	 */
	public boolean removeCallback(final S symbol, final BiConsumer<S, T> callback) throws BitfinexClientException {
		
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);

		if(callbackList == null) {
			throw new BitfinexClientException("Unknown ticker string: " + symbol);
		}			
		
		return callbackList.remove(callback);	
	}
	
	/**
	 * Process a list with event
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleEventsCollection(final S symbol, final Collection<T> elements) {
		
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);
		
		if(callbackList == null) {
			return;
		}
				
		if(callbackList.isEmpty()) {
			return;
		}
		
		// Notify callbacks synchronously, to preserve the order of events
		for (final T element : elements) {
			callbackList.forEach((c) -> {
				c.accept(symbol, element);
			});
		}
	}
	
	/**
	 * Handle a new tick
	 * @param symbol
	 * @param element
	 */
	public void handleEvent(final S symbol, final T element) {
		
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);
		
		if(callbackList == null) {
			return;
		}

		if(callbackList.isEmpty()) {
			return;
		}

		callbackList.forEach((c) -> {
			final Runnable runnable = () -> c.accept(symbol, element);
			executorService.submit(runnable);
		});
	}
}
