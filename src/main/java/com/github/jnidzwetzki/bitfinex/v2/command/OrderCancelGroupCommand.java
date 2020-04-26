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

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrderCancelGroupCommand implements BitfinexOrderCommand {

	/**
	 * The order group
	 */
	private int orderGroup;

	public OrderCancelGroupCommand(final int orderGroup) {
		this.orderGroup = orderGroup;
	}

	@Override
	public String getCommand(BitfinexWebsocketClient client) throws BitfinexCommandException {
		final JSONObject cancelJson = new JSONObject();
		JSONArray gidJsonArray = new JSONArray();
		gidJsonArray.put(orderGroup);
		cancelJson.put("gid", gidJsonArray);
		return "[0, \"oc_multi\", null, " + cancelJson.toString() + "]";
	}

}
