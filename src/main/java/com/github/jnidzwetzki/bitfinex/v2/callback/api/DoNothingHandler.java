package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;

public class DoNothingHandler implements APICallbackHandler {

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final JSONArray jsonArray) throws APIException {
		
	}

}
