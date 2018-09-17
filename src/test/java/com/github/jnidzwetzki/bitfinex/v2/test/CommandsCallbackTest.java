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
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.callback.command.AuthCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.ConnectionHeartbeatCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.SubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.callback.command.UnsubscribedCallback;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class CommandsCallbackTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}
	
	/**
	 * Test the auth callback
	 * @throws BitfinexClientException
	 */
	@Test
	public void testAuthCommandCallback_success() throws BitfinexClientException {
		final String authSuccessJson = "{\"event\":\"auth\",\"status\":\"OK\",\"chanId\":0,\"userId\":1015301,\"auth_id\":\"d5c6a71c-6164-40dd-b57a-92fb59d42975\",\"caps\":{\"orders\":{\"read\":1,\"write\":1},\"account\":{\"read\":1,\"write\":1},\"funding\":{\"read\":1,\"write\":1},\"history\":{\"read\":1,\"write\":1},\"wallets\":{\"read\":1,\"write\":1},\"withdraw\":{\"read\":1,\"write\":1},\"positions\":{\"read\":1,\"write\":1}}}";

		final AuthCallback authCallbackHandler = new AuthCallback();
		authCallbackHandler.onAuthenticationSuccessEvent(capabilities -> {
			Assert.assertEquals(BitfinexApiKeyPermissions.ALL_PERMISSIONS, capabilities);
			Assert.assertTrue(capabilities.toString().length() > 10);
		});
		authCallbackHandler.handleChannelData(new JSONObject(authSuccessJson));
	}
	
	/**
	 * Test the auth callback
	 * @throws BitfinexClientException
	 */
	@Test
	public void testAuthCommandCallback_failed() throws BitfinexClientException {
		final String authFailJson = "{\"event\":\"auth\",\"status\":\"FAILED\",\"chanId\":0}";

		final AuthCallback authCallbackHandler = new AuthCallback();
		authCallbackHandler.onAuthenticationFailedEvent(capabilities -> {
			Assert.assertEquals(BitfinexApiKeyPermissions.NO_PERMISSIONS, capabilities);
			Assert.assertTrue(capabilities.toString().length() > 10);
		});

		authCallbackHandler.handleChannelData(new JSONObject(authFailJson));
	}
	
	/**
	 * Test the pong callback
	 * @throws BitfinexClientException
	 */
	@Test
	public void testPingPongCallback() throws BitfinexClientException {
		final String jsonString = "{\"event\":\"pong\",\"ts\":1515023251265}";
		final JSONObject json = new JSONObject(jsonString);

		final ConnectionHeartbeatCallback connectionHeartbeatCallback = new ConnectionHeartbeatCallback();
		connectionHeartbeatCallback.onHeartbeatEvent(Assert::assertNotNull);
		connectionHeartbeatCallback.handleChannelData(json);
	}
	
	/**
	 * Test the subscribed callback
	 * @throws BitfinexClientException
	 */
	@Test
	public void testSubscribeAndUnsubscribeCallback() throws BitfinexClientException {
		final String subscribeJson = "{\"event\":\"subscribed\",\"channel\":\"ticker\",\"chanId\":30,\"symbol\":\"tNEOUSD\",\"pair\":\"NEOUSD\"}";
		final SubscribedCallback subscribedCallback = new SubscribedCallback();
		subscribedCallback.onSubscribedEvent((chanId, sym) -> {
			Assert.assertEquals((Integer) 30, chanId);
			Assert.assertTrue(sym instanceof BitfinexTickerSymbol);
		});
		subscribedCallback.handleChannelData(new JSONObject(subscribeJson));

		final String unsubscribedJson = "{\"event\":\"unsubscribed\",\"status\":\"OK\",\"chanId\":30}";
		final UnsubscribedCallback unsubscribedCallback = new UnsubscribedCallback();
		unsubscribedCallback.onUnsubscribedChannelEvent(chanId -> {
			Assert.assertEquals((Integer) 30, chanId);
		});
		unsubscribedCallback.handleChannelData(new JSONObject(unsubscribedJson));
	}

}
