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
package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;

public class PositionHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(PositionHandler.class);

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {
		
		logger.info("Got position callback {}", jsonArray.toString());
		
		final JSONArray positions = jsonArray.getJSONArray(2);
		
		// No positons active
		if(positions.length() == 0) {
			notifyLatch(bitfinexApiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (positions.get(0) instanceof JSONArray)) {
			handlePositionCallback(bitfinexApiBroker, positions);
		} else {
			for(int orderPos = 0; orderPos < positions.length(); orderPos++) {
				final JSONArray orderArray = positions.getJSONArray(orderPos);
				handlePositionCallback(bitfinexApiBroker, orderArray);
			}
		}		
		
		notifyLatch(bitfinexApiBroker);
	}

	/**
	 * Notify the snapshot latch
	 * @param bitfinexApiBroker
	 */
	private void notifyLatch(final BitfinexApiBroker bitfinexApiBroker) {
		
		// All snapshots are completed
		final CountDownLatch connectionReadyLatch = bitfinexApiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle a position update
	 * @param bitfinexApiBroker
	 * @param positions
	 */
	private void handlePositionCallback(final BitfinexApiBroker bitfinexApiBroker, final JSONArray positions) {
		final String currencyString = positions.getString(0);
		final BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString(currencyString);
				
		final Position position = new Position(currency);
		position.setStatus(positions.getString(1));
		position.setAmount(positions.getBigDecimal(2));
		position.setBasePrice(positions.getBigDecimal(3));
		position.setMarginFunding(positions.getBigDecimal(4));
		position.setMarginFundingType(positions.optInt(5, -1));
		position.setPl(positions.optBigDecimal(6, BigDecimal.valueOf(-1)));
		position.setPlPercent(positions.optBigDecimal(7, BigDecimal.valueOf(-1)));
		position.setPriceLiquidation(positions.optBigDecimal(8, BigDecimal.valueOf(-1)));
		position.setLeverage(positions.optBigDecimal(9, BigDecimal.valueOf(-1)));						
	}

}
