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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class WalletHandler implements ChannelCallbackHandler {

	private final int channelId;
	private final BitfinexAccountSymbol symbol;

	private BiConsumer<BitfinexAccountSymbol, Collection<BitfinexWallet>> walletConsumer = (s, e) -> {};

	public WalletHandler(int channelId, final BitfinexAccountSymbol symbol) {
		this.channelId = channelId;
		this.symbol = symbol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final String action, final JSONArray payload) throws BitfinexClientException {
		List<BitfinexWallet> wallets = Lists.newArrayList();

		if (payload.isEmpty()) {
			walletConsumer.accept(symbol, wallets);
			return;
		}

		if (payload.get(0) instanceof JSONArray) {
			// snapshot
			for (int walletPos = 0; walletPos < payload.length(); walletPos++) {
				final JSONArray walletArray = payload.getJSONArray(walletPos);
				BitfinexWallet wallet = jsonArrayToWallet(walletArray);
				wallets.add(wallet);
			}
		} else {
			// update
			BitfinexWallet wallet = jsonArrayToWallet(payload);
			wallets.add(wallet);
		}
		walletConsumer.accept(symbol, wallets);
	}

	@Override
	public BitfinexStreamSymbol getSymbol() {
		return symbol;
	}

	@Override
	public int getChannelId() {
		return channelId;
	}

	private BitfinexWallet jsonArrayToWallet(final JSONArray json) {
		final String walletType = json.getString(0);
		final String currency = json.getString(1);
		final BigDecimal balance = json.getBigDecimal(2);
		final BigDecimal unsettledInterest = json.getBigDecimal(3);
		final BigDecimal balanceAvailable = json.optBigDecimal(4, null);
		
		return new BitfinexWallet(walletType, currency, balance, unsettledInterest, balanceAvailable);
	}

	/**
	 * wallet event consumer
	 * @param consumer of event
	 */
	public void onWalletsEvent(BiConsumer<BitfinexAccountSymbol, Collection<BitfinexWallet>> consumer) {
		this.walletConsumer = consumer;
	}
}
