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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.github.jnidzwetzki.bitfinex.v2.entity.FutureOperation;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class FutureOperationRegistry {
	
	/**
	 * The pending futures
	 */
	private final List<FutureOperation> pendingFutures = new CopyOnWriteArrayList<>();
	
	/**
	 * Register a new future
	 * @param futureOperation
	 */
	public void registerFuture(final FutureOperation futureOperation) {
		pendingFutures.add(futureOperation);
	}

	/** 
	 * Handle a subscribe or unsubscribe event
	 */
	public void handleEvent(final BitfinexStreamSymbol symbol) {
		final List<FutureOperation> futuresToFinish = pendingFutures
				.stream()
				.filter(f -> f.getSymbol().equals(symbol))
				.collect(Collectors.toList());
		
		pendingFutures.removeAll(futuresToFinish);
		
		futuresToFinish.forEach(f -> f.setToDone());
	}

}
