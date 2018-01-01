package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.TradeOrderbookConfiguration;

public class TradeOrderbookHandler implements ChannelCallbackHandler {

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final String channelSymbol, final JSONArray jsonArray) throws APIException {
		
		// Example: [13182,1,-0.1]
		try {
			// Snapshots contain multiple Orderbook entries, updates only one
			if(jsonArray.get(0) instanceof JSONArray) {
				for (int pos = 0; pos < jsonArray.length(); pos++) {
					final JSONArray parts = jsonArray.getJSONArray(pos);	
					handleEntry(bitfinexApiBroker, channelSymbol, parts);
				}
			} else {
				handleEntry(bitfinexApiBroker, channelSymbol, jsonArray);
			}
			
		} catch (JSONException e) {
			throw new APIException(e);
		} 
	}

	private void handleEntry(final BitfinexApiBroker bitfinexApiBroker, final String channelSymbol,
			final JSONArray jsonArray) {
		final double price = jsonArray.getNumber(0).doubleValue();
		final double count = jsonArray.getNumber(1).doubleValue();
		final double amount = jsonArray.getNumber(2).doubleValue();
		
		final JSONTokener tokener = new JSONTokener(channelSymbol);
		final JSONObject jsonObject = new JSONObject(tokener);

		final TradeOrderbookConfiguration configuration = TradeOrderbookConfiguration.fromJSON(jsonObject);
		final OrderbookEntry orderbookEntry = new OrderbookEntry(price, count, amount);
		
		bitfinexApiBroker.getTradingOrderbookManager().handleNewOrderbookEntry(configuration, orderbookEntry);
	}

}
