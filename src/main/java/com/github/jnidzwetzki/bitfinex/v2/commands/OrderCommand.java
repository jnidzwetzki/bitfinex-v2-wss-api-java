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
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.exception.CommandException;

public class OrderCommand extends AbstractAPICommand {

	private final BitfinexNewOrder bitfinexOrder;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(OrderCommand.class);

	public OrderCommand(final BitfinexNewOrder bitfinexOrder) {
		this.bitfinexOrder = bitfinexOrder;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		
		final JSONObject orderJson = new JSONObject();
		orderJson.put("type", bitfinexOrder.getOrderType().getBifinexString());
		orderJson.put("symbol", bitfinexOrder.getSymbol().toBitfinexString());
		orderJson.put("amount",  bitfinexOrder.getAmount().toString());
		
		if(bitfinexOrder.getPrice() != null) {
			orderJson.put("price", bitfinexOrder.getPrice().toString());
		}

		if(bitfinexOrder.getPriceTrailing() != null) {
			orderJson.put("price_trailing", bitfinexOrder.getPriceTrailing().toString());
		}

		if(bitfinexOrder.getPriceAuxLimit() != null) {
			orderJson.put("price_aux_limit", bitfinexOrder.getPriceAuxLimit().toString());
		}
		if(bitfinexOrder.getPriceOcoStop() != null) {
			orderJson.put("price_oco_stop", bitfinexOrder.getPriceOcoStop().toString());
		}

		// FIXME: it's no longer valid - not bitfinex exepcts "flags" field
		// FIXME: https://docs.bitfinex.com/v2/reference#ws-input-order-new
		// FIXME: if it works, it's by luck (backward compatibility) - and we don't know how much longer it will work for
		if(bitfinexOrder.isHidden()) {
			orderJson.put("hidden", 1);
		} else {
			orderJson.put("hidden", 0);
		}
		// FIXME: same
		if(bitfinexOrder.isPostOnly()) {
			orderJson.put("postonly", 1);
		}

		if(bitfinexOrder.getClientId() != null) {
			orderJson.put("cid", bitfinexOrder.getClientId());
		}

		if(bitfinexOrder.getClientGroupId() != null) {
			orderJson.put("gid", bitfinexOrder.getClientGroupId());
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"on\", null, ");
		sb.append(orderJson.toString());
		sb.append("]\n");
		
		logger.debug(sb.toString());
		
		return sb.toString();
	}

}
