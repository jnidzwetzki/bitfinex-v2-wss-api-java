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

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;

public class CancelOrderGroupCommand implements BitfinexCommand {

	/**
	 * The order group
	 */
	private int orderGroup;

	public CancelOrderGroupCommand(final int orderGroup) {
		this.orderGroup = orderGroup;
	}

	@Override
	public String getCommand(BitfinexWebsocketClient client) throws BitfinexCommandException {
		final JSONObject cancelJson = new JSONObject();
		cancelJson.put("gid", orderGroup);
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"oc_multi\", null, ");
		sb.append(cancelJson.toString());
		sb.append("]\n");
				
		return sb.toString();
	}

}
