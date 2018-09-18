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

public enum BitfinexOrderFlag {

	// one cancels other order 
	OCO(16384),
	
	// post-only limit order 
	POSTONLY(4096),
	
	// hidden order
	HIDDEN(64),
	
	// Excludes variable rate funding offers
	NO_VR(524288),
	
	// Close position if present
	POS_CLOSE(512),
	
	// Reduce margin position only
	REDUCE_ONLY(1024);
	
	/**
	 * The order flag
	 */
	private final int flag;
	
	private BitfinexOrderFlag(final int flags) {
		this.flag = flags;
	}
	
	/**
	 * Get the order flag
	 * @return
	 */
	public int getFlag() {
		return flag;
	}
}
