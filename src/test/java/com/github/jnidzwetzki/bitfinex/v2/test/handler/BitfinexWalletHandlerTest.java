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
package com.github.jnidzwetzki.bitfinex.v2.test.handler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.WalletHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;


public class BitfinexWalletHandlerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the wallet parsing
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test
	public void testWalletUpdate() throws APIException, InterruptedException {
		
		final String callbackValue = "[0,\"ws\",[\"exchange\",\"ETH\",9,0,null]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable = HashBasedTable.create();

		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final WalletManager walletManager = Mockito.mock(WalletManager.class);
		Mockito.when(bitfinexApiBroker.getWalletManager()).thenReturn(walletManager);

		Mockito.when(walletManager.getWalletTable()).thenReturn(walletTable);

		Assert.assertTrue(walletTable.isEmpty());
		
		final WalletHandler walletHandler = new WalletHandler(0, new BitfinexAccountSymbol("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		walletHandler.onWalletsEvent((a, wallets) -> {
			for (BitfinexWallet wallet : wallets) {
				try {
					Table<BitfinexWallet.Type, String, BitfinexWallet> wt = walletManager.getWalletTable();
					wt.put(wallet.getWalletType(), wallet.getCurrency(), wallet);
				} catch (APIException e) {
					e.printStackTrace();
				}
			}
		});
		walletHandler.handleChannelData(null, jsonArray);

		Assert.assertEquals(1, walletTable.size());
		Assert.assertEquals(9, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getBalance().doubleValue(), DELTA);
		Assert.assertNull(walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getBalanceAvailable());
		Assert.assertEquals(0, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getUnsettledInterest().doubleValue(), DELTA);
	}
	
	/**
	 * Test the wallet parsing
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test
	public void testWalletSnapshot() throws APIException, InterruptedException {
		
		final String callbackValue = "[0,\"ws\",[[\"exchange\",\"ETH\",9,0,null],[\"exchange\",\"USD\",1826.56468323,0,null],[\"margin\",\"USD\",0,0,null],[\"exchange\",\"XRP\",0,0,null],[\"exchange\",\"EOS\",0,0,null],[\"exchange\",\"NEO\",0,0,null],[\"exchange\",\"LTC\",0,0,null],[\"exchange\",\"IOT\",0,0,null],[\"exchange\",\"BTC\",0,0,null]]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable = HashBasedTable.create();

		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final WalletManager walletManager = Mockito.mock(WalletManager.class);
		Mockito.when(bitfinexApiBroker.getWalletManager()).thenReturn(walletManager);

		Mockito.when(walletManager.getWalletTable()).thenReturn(walletTable);

		Assert.assertTrue(walletTable.isEmpty());
		
		final WalletHandler walletHandler = new WalletHandler(0, new BitfinexAccountSymbol("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
		walletHandler.onWalletsEvent((a, wallets) -> {
			for (BitfinexWallet wallet : wallets) {
				try {
					Table<BitfinexWallet.Type, String, BitfinexWallet> wt = walletManager.getWalletTable();
					wt.put(wallet.getWalletType(), wallet.getCurrency(), wallet);
				} catch (APIException e) {
					e.printStackTrace();
				}
			}
		});
		walletHandler.handleChannelData(null, jsonArray);

		Assert.assertEquals(9, walletTable.size());
		
		Assert.assertEquals(9, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getBalance().doubleValue(), DELTA);
		Assert.assertEquals(null, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getBalanceAvailable());
		Assert.assertEquals(0, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getUnsettledInterest().doubleValue(), DELTA);
		
		Assert.assertEquals(1826.56468323, walletTable.get(BitfinexWallet.Type.EXCHANGE, "USD").getBalance().doubleValue(), DELTA);
		Assert.assertEquals(null, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getBalanceAvailable());
		Assert.assertEquals(0, walletTable.get(BitfinexWallet.Type.EXCHANGE, "ETH").getUnsettledInterest().doubleValue(), DELTA);
		
		Assert.assertEquals(0, walletTable.get(BitfinexWallet.Type.MARGIN, "USD").getBalance().doubleValue(), DELTA);
		Assert.assertEquals(null, walletTable.get(BitfinexWallet.Type.MARGIN, "USD").getBalanceAvailable());
		Assert.assertEquals(0, walletTable.get(BitfinexWallet.Type.MARGIN, "USD").getUnsettledInterest().doubleValue(), DELTA);
		
		Assert.assertTrue(walletTable.get(BitfinexWallet.Type.MARGIN, "USD").toString().length() > 0);
	}
	
}
