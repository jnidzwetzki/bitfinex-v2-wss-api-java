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

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.google.common.collect.Table;

public class WalletHandler implements APICallbackHandler {

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {
		
		final JSONArray wallets = jsonArray.getJSONArray(2);
		
		// Snapshot or update
		if(! (wallets.get(0) instanceof JSONArray)) {
			handleWalletcallback(bitfinexApiBroker, wallets);
		} else {
			for(int walletPos = 0; walletPos < wallets.length(); walletPos++) {
				final JSONArray walletArray = wallets.getJSONArray(walletPos);
				handleWalletcallback(bitfinexApiBroker, walletArray);
			}
		}
		
		notifyLatch(bitfinexApiBroker);
	}

	/**
	 * Notify the wallet latch
	 * @param bitfinexApiBroker
	 */
	private void notifyLatch(final BitfinexApiBroker bitfinexApiBroker) {
		
		final CountDownLatch connectionReadyLatch = bitfinexApiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle the callback for a single wallet
	 * @param bitfinexApiBroker 
	 * @param walletArray
	 * @throws APIException 
	 */
	private void handleWalletcallback(final BitfinexApiBroker bitfinexApiBroker, final JSONArray walletArray) throws APIException {
		final String walletType = walletArray.getString(0);
		final String currency = walletArray.getString(1);
		final BigDecimal balance = walletArray.getBigDecimal(2);
		final BigDecimal unsettledInterest = walletArray.getBigDecimal(3);
		final BigDecimal balanceAvailable = walletArray.optBigDecimal(4, BigDecimal.valueOf(-1));
		
		final Wallet wallet = new Wallet(walletType, currency, balance, unsettledInterest, balanceAvailable);

		final WalletManager walletManager = bitfinexApiBroker.getWalletManager();
		final Table<String, String, Wallet> walletTable = walletManager.getWalletTable();
		
		synchronized (walletTable) {
			walletTable.put(walletType, currency, wallet);
			walletTable.notifyAll();
		}
	}

}
