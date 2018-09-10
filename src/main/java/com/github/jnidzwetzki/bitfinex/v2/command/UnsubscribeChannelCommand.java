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
package com.github.jnidzwetzki.bitfinex.v2.command;

import java.util.Objects;
import java.util.function.Function;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.util.BitfinexStreamSymbolToChannelIdResolverAware;

public class UnsubscribeChannelCommand implements BitfinexCommand, BitfinexStreamSymbolToChannelIdResolverAware {

	/**
	 * The channel to unsubscribe
	 */
	private final BitfinexStreamSymbol symbol;
	private Function<BitfinexStreamSymbol, Integer> channelIdResolver = s -> {
	    throw new IllegalStateException("Resolver is not set");
    };

	public UnsubscribeChannelCommand(final BitfinexStreamSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "unsubscribe");
		subscribeJson.put("chanId", channelIdResolver.apply(symbol));

		return subscribeJson.toString();
	}

	@Override
	public void setResolver(Function<BitfinexStreamSymbol, Integer> channelIdResolver) {
		this.channelIdResolver = Objects.requireNonNull(channelIdResolver);
	}
}
