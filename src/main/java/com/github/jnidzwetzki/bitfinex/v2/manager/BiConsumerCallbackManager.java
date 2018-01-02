package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;

public class BiConsumerCallbackManager<S, T> {

	/**
	 * The callbacks
	 */
	private final Map<S, List<BiConsumer<S, T>>> callbacks;
	
	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	public BiConsumerCallbackManager(final ExecutorService executorService) {
		this.executorService = executorService;
		this.callbacks = new HashMap<>();
	}
	
	/**
	 * Register a new callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerCallback(final S symbol, final BiConsumer<S, T> callback) throws APIException {
		
		callbacks.putIfAbsent(symbol, new ArrayList<>());
				
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);
		
		synchronized (callbackList) {
			callbackList.add(callback);	
		}
	}
	
	/**
	 * Remove the a callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeCallback(final S symbol, final BiConsumer<S, T> callback) throws APIException {
		
		if(! callbacks.containsKey(symbol)) {
			throw new APIException("Unknown ticker string: " + symbol);
		}
			
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);
		
		synchronized (callbackList) {
			return callbackList.remove(callback);
		}
	}
	
	/**
	 * Process a list with event
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleEventsList(final S symbol, final List<T> elements) {
		
		final List<BiConsumer<S, T>> callbackList = callbacks.get(symbol);
		
		if(callbackList == null) {
			return;
		}
				
		synchronized(callbackList) {
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

		synchronized(callbackList) {
			if(callbackList.isEmpty()) {
				return;
			}

			callbackList.forEach((c) -> {
				final Runnable runnable = () -> c.accept(symbol, element);
				executorService.submit(runnable);
			});
		}
	}

}
