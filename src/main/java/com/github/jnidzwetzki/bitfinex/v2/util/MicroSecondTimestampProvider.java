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
package com.github.jnidzwetzki.bitfinex.v2.util;

public class MicroSecondTimestampProvider {

	/**
	 * The last currentTimeMillis
	 */
	protected static long lastTimestampMillis = -1;
	
	/**
	 * The counter for this millisecond
	 */
	protected static int counter = 0;
	
	/**
	 * Get a faked micro seconds timestamp. Millisecond collisions are avoided
	 * by adding a faked micro seconds counter to the timestamp
	 * @return 
	 */
	public synchronized static long getNewTimestamp() {
		final long currentMillis = System.currentTimeMillis();
		
		if(currentMillis != lastTimestampMillis) {
			counter = 0;
			lastTimestampMillis = currentMillis;
		}
		
		final long resultValue = currentMillis * 1000 + counter;
		
		counter++;
		
		return resultValue;
	}
	
	/**
	 * Main * Main * Main * Main * Main
	 * @param args
	 */
	public static void main(String[] args) {
		for(int i = 0; i < 10000; i++) {
			System.out.println(MicroSecondTimestampProvider.getNewTimestamp());
		}
	}

}
