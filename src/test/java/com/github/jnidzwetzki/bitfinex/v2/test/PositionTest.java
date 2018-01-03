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
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.PositionHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;


public class PositionTest {
	
	/**
	 * Test the position handler
	 * @throws APIException 
	 */
	@Test
	public void testPositionHandlerUpdate() throws APIException {
		
		final String jsonString = "[0,\"pu\",[\"tETHUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		
		final PositionHandler positionHandler = new PositionHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		Assert.assertTrue(bitfinexApiBroker.getPositionManager().getPositions().isEmpty());
		positionHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		Assert.assertEquals(1, bitfinexApiBroker.getPositionManager().getPositions().size());
	}
	
	/**
	 * Test the position handler
	 * @throws APIException 
	 */
	@Test
	public void testPositionHandlerSnapshot() throws APIException {
		
		final String jsonString = "[0,\"ps\",[[\"tETHUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null], [\"tBTCUSD\",\"ACTIVE\",0.14,713.78,-0.00330012,0,null,null,null,null]]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		
		final PositionHandler positionHandler = new PositionHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		Assert.assertTrue(bitfinexApiBroker.getPositionManager().getPositions().isEmpty());
		positionHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		Assert.assertEquals(2, bitfinexApiBroker.getPositionManager().getPositions().size());
	}
	
	/**
	 * Build a mocked bitfinex connection
	 * @return
	 */
	private BitfinexApiBroker buildMockedBitfinexConnection() {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		
		final PositionManager positionManager = new PositionManager(executorService);
		Mockito.when(bitfinexApiBroker.getPositionManager()).thenReturn(positionManager);
		
		return bitfinexApiBroker;
	}
}
