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

import java.util.concurrent.TimeUnit;

public enum BitfinexCandleTimeFrame {

	MINUTES_1(TimeUnit.MINUTES.toMillis(1), "1m"),
	MINUTES_5(TimeUnit.MINUTES.toMillis(5), "5m"),
	MINUTES_15(TimeUnit.MINUTES.toMillis(15), "15m"),
	MINUTES_30(TimeUnit.MINUTES.toMillis(30), "30m"),
	HOUR_1(TimeUnit.HOURS.toMillis(1), "1h"),
	HOUR_3(TimeUnit.HOURS.toMillis(3), "3h"),
	HOUR_6(TimeUnit.HOURS.toMillis(6), "6h"),
	HOUR_12(TimeUnit.HOURS.toMillis(2), "12h"),
	DAY_1(TimeUnit.DAYS.toMillis(1), "1D"),
	DAY_14(TimeUnit.DAYS.toMillis(14), "14D"),
	MONTH_1(TimeUnit.DAYS.toMillis(30), "1M");

	BitfinexCandleTimeFrame(final long milliseconds, final String bitfinexString) {
		this.milliseconds = milliseconds;
		this.bitfinexString = bitfinexString;
	}
	
	private final long milliseconds;
	
	private final String bitfinexString;

	public long getMilliSeconds() {
		return milliseconds;
	}
	
	public String getBitfinexString() {
		return bitfinexString;
	}
	
	/**
	 * Construct from symbol string
	 * @param symbolString
	 * @return
	 */
	public static BitfinexCandleTimeFrame fromSymbolString(final String symbolString) {
		for (final BitfinexCandleTimeFrame timeframe : BitfinexCandleTimeFrame.values()) {
			if (timeframe.getBitfinexString().equals(symbolString)) {
				return timeframe;
			}
		}
		throw new IllegalArgumentException("Unable to find timeframe for: " + symbolString);
	}
}
