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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.CalculateCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;

public class WalletManager extends AbstractManager {

	/**
	 * WalletTable aggregator - key(Wallet-Type, Currency) = Wallet
	 */
	private final Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable;

	public WalletManager(final BitfinexWebsocketClient client, final ExecutorService executorService) {
		super(client, executorService);
		this.walletTable = HashBasedTable.create();
		client.getCallbacks().onMyWalletEvent((account, wallets) -> wallets.forEach(wallet -> {
            try {
                Table<BitfinexWallet.Type, String, BitfinexWallet> walletTable = getWalletTable();
                synchronized (walletTable) {
                    walletTable.put(wallet.getWalletType(), wallet.getCurrency(), wallet);
                    walletTable.notifyAll();
                }
            } catch (BitfinexClientException e) {
                e.printStackTrace();
            }
        }));
	}

	/**
	 * Get all wallets
	 * @return
	 * @throws BitfinexClientException
	 */
	public Collection<BitfinexWallet> getWallets() throws BitfinexClientException {

		throwExceptionIfUnauthenticated();

		synchronized (walletTable) {
			return Collections.unmodifiableCollection(walletTable.values());
		}
	}

	/**
	 * Get all wallets
	 * @return
	 * @throws BitfinexClientException
	 */
	public Table<BitfinexWallet.Type, String, BitfinexWallet> getWalletTable() throws BitfinexClientException {
		return walletTable;
	}

	/**
	 * Throw a new exception if called on a unauthenticated connection
	 * @throws BitfinexClientException
	 */
	private void throwExceptionIfUnauthenticated() throws BitfinexClientException {
		if(! client.isAuthenticated()) {
			throw new BitfinexClientException("Unable to perform operation on an unauthenticated connection");
		}
	}

	/**
	 * Calculate the wallet margin balance for the given currency (e.g., BTC)
	 *
	 * @param symbol
	 * @throws BitfinexClientException
	 */
	public void calculateWalletMarginBalance(final String symbol) throws BitfinexClientException {
		throwExceptionIfUnauthenticated();

		client.sendCommand(new CalculateCommand("wallet_margin_" + symbol));
	}

	/**
	 * Calculate the wallet funding balance for the given currency (e.g., BTC)
	 *
	 * @param symbol
	 * @throws BitfinexClientException
	 */
	public void calculateWalletFundingBalance(final String symbol) throws BitfinexClientException {
		throwExceptionIfUnauthenticated();

		client.sendCommand(new CalculateCommand("wallet_funding_" + symbol));
	}

}
