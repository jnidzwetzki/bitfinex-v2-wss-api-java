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

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackListeners;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class FutureOperation {
	
	/**
	 * The is done latch
	 */
	private final CountDownLatch doneLatch = new CountDownLatch(1);
	
	/**
	 * The event listener
	 */
	private final Closeable closeable;

	public FutureOperation(final BitfinexApiCallbackListeners callbackListeners, 
			final BitfinexStreamSymbol symbol, final boolean subscribeEvent) {
		
		// FIXME: Find a good way to unsubscribe this event (currently performed in finalizer)
		final Consumer<BitfinexStreamSymbol> listener = (s) -> {
			if(s.equals(symbol)) {
				doneLatch.countDown();
			}
		};
		
		if(subscribeEvent) {
			closeable = callbackListeners.onSubscribeChannelEvent(listener);
		} else {
			closeable = callbackListeners.onUnsubscribeChannelEvent(listener);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeable.close();
	}

	public boolean isDone() {
		return doneLatch.getCount() == 0;
	}

	public void waitForCompletion() throws InterruptedException, ExecutionException {
		doneLatch.await();
	}

	public void waitForCompletion(final long timeout, final TimeUnit unit) 
			throws InterruptedException, ExecutionException, TimeoutException {
		doneLatch.await(timeout, unit);
	}

}
