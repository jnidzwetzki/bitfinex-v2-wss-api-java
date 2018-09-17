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
package com.github.jnidzwetzki.bitfinex.v2.entity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class FutureOperation {
	
	/**
	 * The is done latch
	 */
	private final CountDownLatch doneLatch = new CountDownLatch(1);
	
	/**
	 * The symbol
	 */
	private final BitfinexStreamSymbol symbol;

	public FutureOperation(final BitfinexStreamSymbol symbol) {
		this.symbol = symbol;
	}
	
	/**
	 * Get the stream symbol
	 * @return
	 */
	public BitfinexStreamSymbol getSymbol() {
		return symbol;
	}

	/**
	 * Is the operation done?
	 * @return
	 */
	public boolean isDone() {
		return doneLatch.getCount() == 0;
	}
	
	/**
	 * The the future to done
	 */
	public void setToDone() {
		if(! isDone()) {
			doneLatch.countDown();
		}
	}

	/**
	 * Wait for the completion of the operation
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void waitForCompletion() throws InterruptedException, ExecutionException {
		doneLatch.await();
	}

	/**
	 * Wait for the completion of the operation (timed version)
	 * @param timeout
	 * @param unit
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public void waitForCompletion(final long timeout, final TimeUnit unit) 
			throws InterruptedException, ExecutionException, TimeoutException {
		doneLatch.await(timeout, unit);
	}

}
