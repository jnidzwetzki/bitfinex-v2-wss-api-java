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

import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.AccountInfoHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;


public class AccountInfoHandlerTest {
	
	@Test
	public void testAccountInfoHandler1() throws InterruptedException {
		final BitfinexAccountSymbol symbol = BitfinexSymbols.account("abc", BitfinexApiKeyPermissions.NO_PERMISSIONS);
		final AccountInfoHandler accountInfoHandler = new AccountInfoHandler(0, symbol);
		Assert.assertEquals(0, accountInfoHandler.getChannelId());
		Assert.assertEquals(symbol, accountInfoHandler.getSymbol());
		
		// Default handler
		accountInfoHandler.handleChannelData("hb", new JSONArray());
		
		// Error handler
		accountInfoHandler.handleChannelData("hb", new JSONArray("[ERROR]"));

		// Custom handler
		final CountDownLatch hbLatch = new CountDownLatch(1);
		accountInfoHandler.onHeartbeatEvent((h) -> hbLatch.countDown());
		accountInfoHandler.handleChannelData("hb", new JSONArray());
		
		hbLatch.await();
	}
	

	@Test
	public void testAccountInfoHandler2() throws InterruptedException {
		final BitfinexAccountSymbol symbol = BitfinexSymbols.account("abc", BitfinexApiKeyPermissions.NO_PERMISSIONS);
		final AccountInfoHandler accountInfoHandler = new AccountInfoHandler(0, symbol);
		
		accountInfoHandler.onHeartbeatEvent((h) -> {});
		accountInfoHandler.onOrderNotification((v1, v2) -> {});
		accountInfoHandler.onPositionsEvent((v1, v2) -> {});
		accountInfoHandler.onSubmittedOrderEvent((v1, v2) -> {});
		accountInfoHandler.onTradeEvent((v1, v2) -> {});
		accountInfoHandler.onWalletsEvent((v1, v2) -> {});
	}
	
}
