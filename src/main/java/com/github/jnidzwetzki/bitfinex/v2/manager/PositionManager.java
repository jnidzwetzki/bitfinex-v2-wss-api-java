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

import com.github.jnidzwetzki.bitfinex.v2.entity.Position;

public class PositionManager extends AbstractSimpleCallbackManager<Position> {

	/**
	 * The positions
	 */
	private final List<Position> positions;

	public PositionManager(final ExecutorService executorService) {
		super(executorService);
		this.positions = new ArrayList<>();
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
	 * @param exchangeOrder
	 */
	public void updatePosition(final Position position) {
		
		synchronized (positions) {
			// Replace position
			positions.removeIf(p -> p.getCurreny() == position.getCurreny());
			positions.add(position);
			positions.notifyAll();
		}
		
		notifyCallbacks(position);
	}
	
	/**
	 * Get the positions
	 * @return
	 */
	public List<Position> getPositions() {
		synchronized (positions) {
			return positions;
		}
	}
	
}
