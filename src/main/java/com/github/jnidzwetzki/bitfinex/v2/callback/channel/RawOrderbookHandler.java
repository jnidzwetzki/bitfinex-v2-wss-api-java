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

import org.json.JSONArray;
import org.json.JSONException;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class RawOrderbookHandler implements ChannelCallbackHandler {

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {
		
		final RawOrderbookConfiguration configuration = (RawOrderbookConfiguration) channelSymbol;
		
		// Example: [13182,1,-0.1]
		try {
			// Snapshots contain multiple Orderbook entries, updates only one
			if(jsonArray.get(0) instanceof JSONArray) {
				for (int pos = 0; pos < jsonArray.length(); pos++) {
					final JSONArray parts = jsonArray.getJSONArray(pos);	
					handleEntry(bitfinexApiBroker, configuration, parts);
				}
			} else {
				handleEntry(bitfinexApiBroker, configuration, jsonArray);
			}
			
		} catch (JSONException e) {
			throw new APIException(e);
		} 
	}

	/**
	 * Handle a new orderbook entry
	 * @param bitfinexApiBroker
	 * @param configuration
	 * @param jsonArray
	 */
	private void handleEntry(final BitfinexApiBroker bitfinexApiBroker, 
			final RawOrderbookConfiguration configuration,
			final JSONArray jsonArray) {
		
		final long orderId = jsonArray.getNumber(0).longValue();
		final BigDecimal price = jsonArray.getBigDecimal(1);
		final BigDecimal amount = jsonArray.getBigDecimal(2);
		
		final RawOrderbookEntry orderbookEntry = new RawOrderbookEntry(orderId, price, amount);
		
		bitfinexApiBroker.getRawOrderbookManager().handleNewOrderbookEntry(configuration, orderbookEntry);
	}

}
