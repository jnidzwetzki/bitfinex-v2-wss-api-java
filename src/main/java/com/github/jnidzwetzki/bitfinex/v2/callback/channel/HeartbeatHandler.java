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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.util.function.Consumer;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class HeartbeatHandler implements ChannelCallbackHandler {

	private final static Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

	private final int channelId;
	private final BitfinexAccountSymbol symbol;

	private Consumer<Long> heartbeatConsumer = l -> {};

	public HeartbeatHandler(int channelId, final BitfinexAccountSymbol symbol) {
		this.channelId = channelId;
		this.symbol = symbol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final String action, final JSONArray jsonArray) throws BitfinexClientException {
		logger.debug("Got connection heartbeat");
		heartbeatConsumer.accept(System.currentTimeMillis());
	}

	@Override
	public BitfinexStreamSymbol getSymbol() {
		return symbol;
	}

	@Override
	public int getChannelId() {
		return channelId;
	}

	public void onHeartbeatEvent(Consumer<Long> heartbeatConsumer) {
		this.heartbeatConsumer = heartbeatConsumer;
	}
}
