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
package com.github.jnidzwetzki.bitfinex.v2.callback.command;

import java.util.function.Consumer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;

public class ConfCallback implements CommandCallbackHandler {

	private final static Logger logger = LoggerFactory.getLogger(ConfCallback.class);

	private Consumer<Integer> connectionFeatureConsumer = i -> {};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleChannelData(final JSONObject jsonObject) throws APIException {
		final String status = jsonObject.getString("status");
		if (!status.equals("OK")) {
			logger.info("Got wrong state back {}", status);
		}
		// Got message: {"event":"conf","status":"OK","flags":65536}
		if (jsonObject.has("flags")) {
			final int features = jsonObject.getInt("flags");
			this.connectionFeatureConsumer.accept(features);
		} else {
			this.connectionFeatureConsumer.accept(0);
		}
	}

	/**
	 * connection feature event consumer
	 * @param consumer of event
	 */
	public void onConnectionFeatureEvent(Consumer<Integer> consumer) {
		this.connectionFeatureConsumer = consumer;
	}
}
