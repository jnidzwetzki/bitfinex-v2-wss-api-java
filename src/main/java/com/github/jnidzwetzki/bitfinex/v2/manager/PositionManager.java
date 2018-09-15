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

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;

public class PositionManager extends SimpleCallbackManager<BitfinexPosition> {

	/**
	 * The positions
	 */
	private final List<BitfinexPosition> positions;

	public PositionManager(final BitfinexWebsocketClient client, final ExecutorService executorService) {
		super(executorService, client);
		this.positions = new ArrayList<>();
		client.getCallbacks().onMyPositionEvent((account, positions) -> positions.forEach(this::updatePosition));
	}

	/**
	 * Clear all orders
	 */
	public void clear() {
		synchronized (positions) {
			positions.clear();	
		}
	}
	
	/**
	 * Update a exchange order
	 * @param position
	 */
	public void updatePosition(final BitfinexPosition position) {
		
		synchronized (positions) {
			// Replace position
			positions.removeIf(p -> p.getCurrencyPair() == position.getCurrencyPair());
			positions.add(position);
			positions.notifyAll();
		}
		
		notifyCallbacks(position);
	}
	
	/**
	 * Get the positions
	 * @return
	 */
	public List<BitfinexPosition> getPositions() {
		synchronized (positions) {
			return positions;
		}
	}
	
}
