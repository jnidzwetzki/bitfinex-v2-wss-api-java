package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.time.ZonedDateTime;

import org.json.JSONArray;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.Const;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class TickHandler implements ChannelCallbackHandler {

	/**
	 * Handle a tick callback
	 * @param channel
	 * @param subarray
	 */
	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, 
			final BitfinexStreamSymbol channelSymbol, final JSONArray jsonArray) throws APIException {

		final BitfinexCurrencyPair currencyPair = (BitfinexCurrencyPair) channelSymbol;
		
		// 0 = BID
		// 2 = ASK
		// 6 = Price
		final double price = jsonArray.getDouble(6);
		
		// Volume is set to 0, because the ticker contains only the daily volume
		final Tick tick = new BaseTick(ZonedDateTime.now(Const.BITFINEX_TIMEZONE), price, price, 
				price, price, 0);
		
		bitfinexApiBroker.getquoteManager().handleNewTick(currencyPair, tick);
	}
}
