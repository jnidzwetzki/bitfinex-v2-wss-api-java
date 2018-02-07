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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.AuthCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ConnectionHeartbeatCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.SubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.UnsubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;

public class CommandsCallbackTest {
	
	/**
	 * Test the auth callback
	 * @throws APIException
	 */
	@Test
	public void testAuthCommandCallback1() throws APIException {
		final String authCallback = "{\"event\":\"auth\",\"status\":\"OK\",\"chanId\":0,\"userId\":1015301,\"auth_id\":\"d5c6a71c-6164-40dd-b57a-92fb59d42975\",\"caps\":{\"orders\":{\"read\":1,\"write\":1},\"account\":{\"read\":1,\"write\":0},\"funding\":{\"read\":1,\"write\":1},\"history\":{\"read\":1,\"write\":0},\"wallets\":{\"read\":1,\"write\":1},\"withdraw\":{\"read\":0,\"write\":0},\"positions\":{\"read\":1,\"write\":1}}}";
		final BitfinexApiBroker bitfinexApiBroker = new BitfinexApiBroker();
		final JSONObject jsonObject = new JSONObject(authCallback);
		
		final AuthCallbackHandler authCallbackHandler = new AuthCallbackHandler();
		Assert.assertFalse(bitfinexApiBroker.isAuthenticated());
		Assert.assertEquals(ConnectionCapabilities.NO_CAPABILITIES, bitfinexApiBroker.getCapabilities());
		authCallbackHandler.handleChannelData(bitfinexApiBroker, jsonObject);
		Assert.assertTrue(bitfinexApiBroker.isAuthenticated());
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();

		Assert.assertTrue(capabilities.isHavingOrdersReadCapability());
		Assert.assertTrue(capabilities.isHavingOrdersWriteCapability());
		Assert.assertTrue(capabilities.isHavingAccountReadCapability());
		Assert.assertFalse(capabilities.isHavingAccountWriteCapability());
		Assert.assertTrue(capabilities.isHavingFundingReadCapability());
		Assert.assertTrue(capabilities.isHavingFundingWriteCapability());
		Assert.assertTrue(capabilities.isHavingHistoryReadCapability());
		Assert.assertFalse(capabilities.isHavingHistoryWriteCapability());
		Assert.assertTrue(capabilities.isHavingWalletsReadCapability());
		Assert.assertTrue(capabilities.isHavingWalletsWriteCapability());
		Assert.assertFalse(capabilities.isHavingWithdrawReadCapability());
		Assert.assertFalse(capabilities.isHavingWithdrawWriteCapability());
		Assert.assertTrue(capabilities.isHavingPositionReadCapability());
		Assert.assertTrue(capabilities.isHavingPositionWriteCapability());
		
		Assert.assertTrue(capabilities.toString().length() > 10);
	}
	
	/**
	 * Test the auth callback
	 * @throws APIException
	 */
	@Test
	public void testAuthCommandCallback2() throws APIException {
		final String authCallback = "{\"event\":\"auth\",\"status\":\"FAILED\",\"chanId\":0,}";
		final BitfinexApiBroker bitfinexApiBroker = Mockito.spy(BitfinexApiBroker.class);
		final JSONObject jsonObject = new JSONObject(authCallback);
		
		final AuthCallbackHandler authCallbackHandler = new AuthCallbackHandler();
		Assert.assertFalse(bitfinexApiBroker.isAuthenticated());
		authCallbackHandler.handleChannelData(bitfinexApiBroker, jsonObject);
		Assert.assertFalse(bitfinexApiBroker.isAuthenticated());
	}
	
	/**
	 * Test the pong callback
	 * @throws APIException
	 */
	@Test
	public void testConnectionHeartbeat() throws APIException {
		final String jsonString = "{\"event\":\"pong\",\"ts\":1515023251265}";
		final JSONObject jsonObject = new JSONObject(jsonString);

		final BitfinexApiBroker bitfinexApiBroker = new BitfinexApiBroker();
		final long oldHeartbeat = bitfinexApiBroker.getLastHeatbeat().get();
		final ConnectionHeartbeatCallback connectionHeartbeatCallback = new ConnectionHeartbeatCallback();
		connectionHeartbeatCallback.handleChannelData(bitfinexApiBroker, jsonObject);
		final long newHeartbeat = bitfinexApiBroker.getLastHeatbeat().get();
		Assert.assertTrue(oldHeartbeat < newHeartbeat);
	}
	
	/**
	 * Test the subscribed callback
	 * @throws APIException 
	 */
	@Test
	public void testSubscribeAndUnsubscribeCallback() throws APIException {
		final String jsonString = "{\"event\":\"subscribed\",\"channel\":\"ticker\",\"chanId\":30,\"symbol\":\"tNEOUSD\",\"pair\":\"NEOUSD\"}";
		final JSONObject jsonObject = new JSONObject(jsonString);

		final BitfinexApiBroker bitfinexApiBroker = new BitfinexApiBroker();
		Assert.assertTrue(bitfinexApiBroker.getFromChannelSymbolMap(30) == null);
		
		final SubscribedCallback subscribedCallback = new SubscribedCallback();
		subscribedCallback.handleChannelData(bitfinexApiBroker, jsonObject);
		Assert.assertTrue(bitfinexApiBroker.getFromChannelSymbolMap(30) instanceof BitfinexTickerSymbol);
	
		final String unsubscribedJsonString = "{\"event\":\"unsubscribed\",\"status\":\"OK\",\"chanId\":30}";
		final JSONObject jsonUnsubscribedObject = new JSONObject(unsubscribedJsonString);
		final UnsubscribedCallback unsubscribedCallback = new UnsubscribedCallback();
		unsubscribedCallback.handleChannelData(bitfinexApiBroker, jsonUnsubscribedObject);
		
		Assert.assertTrue(bitfinexApiBroker.getFromChannelSymbolMap(30) == null);
	}

}
