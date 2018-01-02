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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.Const;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class CandlestickHandler implements ChannelCallbackHandler {

	/**
	 * Handle a candlestick callback
	 * @param channel
	 * @param subarray
	 */
	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {

		// channel symbol trade:1m:tLTCUSD
		final List<Tick> ticksBuffer = new ArrayList<>();
		
		// Snapshots contain multiple Bars, Updates only one
		if(jsonArray.get(0) instanceof JSONArray) {
			for (int pos = 0; pos < jsonArray.length(); pos++) {
				final JSONArray parts = jsonArray.getJSONArray(pos);	
				parseCandlestick(ticksBuffer, parts);
			}
		} else {
			parseCandlestick(ticksBuffer, jsonArray);
		}
		
		ticksBuffer.sort((t1, t2) -> t1.getEndTime().compareTo(t2.getEndTime()));

		final BitfinexCandlestickSymbol candlestickSymbol = (BitfinexCandlestickSymbol) channelSymbol;
		bitfinexApiBroker.getQuoteManager().handleCandlestickList(candlestickSymbol, ticksBuffer);
	}

	/**
	 * Parse a candlestick from JSON result
	 */
	private void parseCandlestick(final List<Tick> ticksBuffer, final JSONArray parts) {
		// 0 = Timestamp, 1 = Open, 2 = Close, 3 = High, 4 = Low,  5 = Volume
		final Instant i = Instant.ofEpochMilli(parts.getLong(0));
		final ZonedDateTime withTimezone = ZonedDateTime.ofInstant(i, Const.BITFINEX_TIMEZONE);
		
		final double open = parts.getDouble(1);
		final double close = parts.getDouble(2);
		final double high = parts.getDouble(3);
		final double low = parts.getDouble(4);
		final double volume = parts.getDouble(5);
		
		final Tick tick = new BaseTick(withTimezone, open, high, low, close, volume);
		ticksBuffer.add(tick);
	}
}
