package com.github.jnidzwetzki.bitfinex.v2.commands;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;

public class UnsubscribeChannelCommand extends AbstractAPICommand {

	/**
	 * The channel to unsubscribe
	 */
	private final int channel;

	public UnsubscribeChannelCommand(final int channel) {
		this.channel = channel;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "unsubscribe");
		subscribeJson.put("chanId", channel);
		
		return subscribeJson.toString();
	}

}
