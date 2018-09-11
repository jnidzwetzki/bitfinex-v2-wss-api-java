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

import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.Sets;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexConnectionFeature;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.SetConnectionFeaturesCommand;

public class ConnectionFeatureManager extends AbstractManager {
	
	/**
	 * The connection features
	 */
	private final Set<BitfinexConnectionFeature> connectionFeatures;
	
	/**
	 * The active connection features 
	 * (we got a status update for our requested features)
	 */
	private int activeConnectionFeatures;
	
	
	public ConnectionFeatureManager(final BitfinexWebsocketClient client,
			final ExecutorService executorService) {
		super(client, executorService);
		
		this.connectionFeatures = Sets.newConcurrentHashSet();
		this.activeConnectionFeatures = 0;
	}

	/**
	 * Enable a connection feature
	 * @param feature
	 */
	public void enableConnectionFeature(final BitfinexConnectionFeature feature) {
		connectionFeatures.add(feature);
		applyConnectionFeatures();
	}
	
	/**
	 * Disable a connection feature
	 * @param feature
	 */
	public void disableConnectionFeature(final BitfinexConnectionFeature feature) {
		connectionFeatures.remove(feature);
		applyConnectionFeatures();
	}

	/**
	 * Is the given connection feature enabled?
	 * @param feature
	 * @return
	 */
	public boolean isConnectionFeatureEnabled(final BitfinexConnectionFeature feature) {
		return connectionFeatures.contains(feature);
	}
	
	/**
	 * Get the active connection features
	 * @return
	 */
	public int getActiveConnectionFeatures() {
		return activeConnectionFeatures;
	}
	
	/**
	 * Is the given connection feature active?
	 * @param feature
	 * @return
	 */
	public boolean isConnectionFeatureActive(final BitfinexConnectionFeature feature) {
		return (activeConnectionFeatures | feature.getFeatureFlag()) == activeConnectionFeatures;
	}
	
	/**
	 * Set the active connection features
	 * @param activeConnectionFeatures
	 */
	public void setActiveConnectionFeatures(final int activeConnectionFeatures) {
		this.activeConnectionFeatures = activeConnectionFeatures;
	}
	
	/**
	 * Apply the set connection features to connection
	 */
	public void applyConnectionFeatures() {
		final SetConnectionFeaturesCommand apiCommand = new SetConnectionFeaturesCommand(connectionFeatures);
		client.sendCommand(apiCommand);
	}
}
