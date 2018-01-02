package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public interface ChannelCallbackHandler {
	
	/**
	 * Handle data for the channel
	 * @param bitfinexApiBroker
	 * @param jsonArray
	 * @throws APIException 
	 */
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker,  
			final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) 
			throws APIException;

}
