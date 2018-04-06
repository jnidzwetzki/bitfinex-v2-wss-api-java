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
package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;

public class OrderCommand extends AbstractAPICommand {

	private final BitfinexOrder bitfinexOrder;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(OrderCommand.class);

	public OrderCommand(final BitfinexOrder bitfinexOrder) {
		this.bitfinexOrder = bitfinexOrder;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		
		final JSONObject orderJson = new JSONObject();
		orderJson.put("cid", bitfinexOrder.getCid());
		orderJson.put("type", bitfinexOrder.getType().getBifinexString());
		orderJson.put("symbol", bitfinexOrder.getSymbol().toBitfinexString());
		orderJson.put("amount",  bitfinexOrder.getAmount());
		
		if(bitfinexOrder.getPrice() != null) {
			orderJson.put("price", bitfinexOrder.getPrice());
		}

		if(bitfinexOrder.getPriceTrailing() != null) {
			orderJson.put("price_trailing", bitfinexOrder.getPriceTrailing());
		}
		
		if(bitfinexOrder.getPriceAuxLimit() != null) {
			orderJson.put("price_aux_limit", bitfinexOrder.getPriceAuxLimit());
		}
		
		if(bitfinexOrder.isHidden()) {
			orderJson.put("hidden", 1);
		} else {
			orderJson.put("hidden", 0);
		}
		
		if(bitfinexOrder.isPostOnly()) {
			orderJson.put("postonly", 1);
		}
		
		if(bitfinexOrder.getGroupId() > 0) {
			orderJson.put("gid", bitfinexOrder.getGroupId());
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"on\", null, ");
		sb.append(orderJson.toString());
		sb.append("]\n");
		
		logger.debug(sb.toString());
		
		return sb.toString();
	}

}
