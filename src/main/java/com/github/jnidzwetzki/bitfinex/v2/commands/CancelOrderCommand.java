package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;

public class CancelOrderCommand extends AbstractAPICommand {

	/**
	 * The cid
	 */
	private long id;

	public CancelOrderCommand(final long id) {
		this.id = id;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		
		final JSONObject cancelJson = new JSONObject();
		cancelJson.put("id", id);
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"oc\", null, ");
		sb.append(cancelJson.toString());
		sb.append("]\n");
				
		return sb.toString();
	}

}
