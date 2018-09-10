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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;

public class SimpleCallbackManager<T> extends AbstractManager {
	
	/**
	 * The order callbacks
	 */
	private final List<Consumer<T>> callbacks;
	
	public SimpleCallbackManager(final ExecutorService executorService, 
			final BitfinexWebsocketClient client) {
		super(client, executorService);
		this.callbacks = new ArrayList<>();
	}
	
	/**
	 * Add a order callback
	 * @param callback
	 */
	public void registerCallback(final Consumer<T> callback) {
		synchronized (callbacks) {
			callbacks.add(callback);
		}
	}
	
	/**
	 * Remove a order callback
	 * @param callback
	 * @return
	 */
	public boolean removeCallback(final Consumer<T> callback) {
		synchronized (callbacks) {
			return callbacks.remove(callback);
		}
	}
	
	/**
	 * Update a exchange order
	 * @param exchangeOrder
	 */
	public void notifyCallbacks(final T exchangeOrder) {

		// Notify callbacks async		
		if(callbacks == null) {
			return;
		}
				
		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}
			
			callbacks.forEach((c) -> {
				final Runnable runnable = () -> c.accept(exchangeOrder);
				executorService.submit(runnable);
			});
		}
	}
}
