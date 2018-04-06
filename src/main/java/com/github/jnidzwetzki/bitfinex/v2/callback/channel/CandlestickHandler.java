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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
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
		final List<BitfinexTick> ticksBuffer = new ArrayList<>();
		
		// Snapshots contain multiple Bars, Updates only one
		if(jsonArray.get(0) instanceof JSONArray) {
			for (int pos = 0; pos < jsonArray.length(); pos++) {
				final JSONArray parts = jsonArray.getJSONArray(pos);	
				parseCandlestick(ticksBuffer, parts);
			}
		} else {
			parseCandlestick(ticksBuffer, jsonArray);
		}
		
		// Use natural ordering
		ticksBuffer.sort(null);
		
		final BitfinexCandlestickSymbol candlestickSymbol = (BitfinexCandlestickSymbol) channelSymbol;
		bitfinexApiBroker.getQuoteManager().handleCandlestickList(candlestickSymbol, ticksBuffer);
	}

	/**
	 * Parse a candlestick from JSON result
	 */
	private void parseCandlestick(final List<BitfinexTick> ticksBuffer, final JSONArray parts) {
		
		// 0 = Timestamp, 1 = Open, 2 = Close, 3 = High, 4 = Low,  5 = Volume
		final long timestamp = parts.getLong(0);
		final BigDecimal open = parts.getBigDecimal(1);
		final BigDecimal close = parts.getBigDecimal(2);
		final BigDecimal high = parts.getBigDecimal(3);
		final BigDecimal low = parts.getBigDecimal(4);
		final BigDecimal volume = parts.getBigDecimal(5);
		
		final BitfinexTick tick = new BitfinexTick(timestamp, open, close, high, low, volume);
		ticksBuffer.add(tick);
	}
}
