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
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;

public class WalletHandler implements APICallbackHandler {

	private Consumer<Collection<Wallet>> walletConsumer = w -> {};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final JSONArray jsonArray) throws APIException {
		final JSONArray json = jsonArray.getJSONArray(2);
		List<Wallet> wallets = Lists.newArrayList();

		if (json.length() == 0) {
			walletConsumer.accept(wallets);
			return;
		}

		if (json.get(0) instanceof JSONArray) {
			// snapshot
			for (int walletPos = 0; walletPos < json.length(); walletPos++) {
				final JSONArray walletArray = json.getJSONArray(walletPos);
				Wallet wallet = jsonArrayToWallet(walletArray);
				wallets.add(wallet);
			}
		} else {
			// update
			Wallet wallet = jsonArrayToWallet(json);
			wallets.add(wallet);
		}
		walletConsumer.accept(wallets);
	}

	private Wallet jsonArrayToWallet(final JSONArray json) {
		final String walletType = json.getString(0);
		final String currency = json.getString(1);
		final BigDecimal balance = json.getBigDecimal(2);
		final BigDecimal unsettledInterest = json.getBigDecimal(3);
		final BigDecimal balanceAvailable = json.optBigDecimal(4, null);
		
		return new Wallet(walletType, currency, balance, unsettledInterest, balanceAvailable);
	}

	/**
	 * wallet event consumer
	 * @param consumer of event
	 */
	public void onWalletsEvent(Consumer<Collection<Wallet>> consumer) {
		this.walletConsumer = consumer;
	}
}
