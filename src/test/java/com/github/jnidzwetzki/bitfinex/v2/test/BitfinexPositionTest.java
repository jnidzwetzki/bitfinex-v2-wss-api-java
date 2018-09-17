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
package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.PositionHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;


public class BitfinexPositionTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
		}
		BitfinexCurrencyPair.registerDefaults();	
	}

	/**
	 * Test the position handler
	 * @throws BitfinexClientException
	 */
	@Test
	public void testPositionHandlerUpdate1() throws BitfinexClientException {
		
		final String jsonString = "[0,\"pu\",[\"tETHUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null]]";
		final JSONArray jsonArray = new JSONArray(jsonString);

		final BitfinexWebsocketClient bitfinexApiBroker = buildMockedBitfinexConnection();
		final PositionHandler positionHandler = new PositionHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		positionHandler.onPositionsEvent((a, positions) -> {
			for (BitfinexPosition position : positions) {
				bitfinexApiBroker.getPositionManager().updatePosition(position);
			}
		});

		Assert.assertTrue(bitfinexApiBroker.getPositionManager().getPositions().isEmpty());
		positionHandler.handleChannelData("pu", jsonArray.getJSONArray(2));
		Assert.assertEquals(1, bitfinexApiBroker.getPositionManager().getPositions().size());
	}
	
	/**
	 * Test the position handler - with null funding type
	 * @throws BitfinexClientException
	 */
	@Test
	public void testPositionHandlerUpdate2() throws BitfinexClientException {
		
		final String jsonString = "[0,\"pu\",[\"tETHUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,null,null,null,null,null]]";
		final JSONArray jsonArray = new JSONArray(jsonString);

		final BitfinexWebsocketClient bitfinexApiBroker = buildMockedBitfinexConnection();
		final PositionHandler positionHandler = new PositionHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		positionHandler.onPositionsEvent((a, positions) -> {
			for (BitfinexPosition position : positions) {
				bitfinexApiBroker.getPositionManager().updatePosition(position);
			}
		});

		Assert.assertTrue(bitfinexApiBroker.getPositionManager().getPositions().isEmpty());
		positionHandler.handleChannelData("pu", jsonArray.getJSONArray(2));
		Assert.assertEquals(1, bitfinexApiBroker.getPositionManager().getPositions().size());
	}
	
	/**
	 * Test the position handler
	 * @throws BitfinexClientException
	 */
	@Test
	public void testPositionHandlerSnapshot() throws BitfinexClientException {
		
		final String jsonString = "[0,\"ps\",[[\"tETHUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null], [\"tBTCUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null]]]";
		final JSONArray jsonArray = new JSONArray(jsonString);

		final BitfinexWebsocketClient bitfinexApiBroker = buildMockedBitfinexConnection();
		final PositionHandler positionHandler = new PositionHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		positionHandler.onPositionsEvent((a, positions) -> {
			for (BitfinexPosition position : positions) {
				bitfinexApiBroker.getPositionManager().updatePosition(position);
			}
		});

		Assert.assertTrue(bitfinexApiBroker.getPositionManager().getPositions().isEmpty());
		positionHandler.handleChannelData("ps", jsonArray.getJSONArray(2));
		Assert.assertEquals(2, bitfinexApiBroker.getPositionManager().getPositions().size());
	}
	
	/**
	 * Build a mocked bitfinex connection
	 * @return
	 */
	private BitfinexWebsocketClient buildMockedBitfinexConnection() {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getCallbacks()).thenReturn(new BitfinexApiCallbackRegistry());

		final PositionManager positionManager = new PositionManager(bitfinexApiBroker, executorService);
		Mockito.when(bitfinexApiBroker.getPositionManager()).thenReturn(positionManager);
		
		return bitfinexApiBroker;
	}
}
