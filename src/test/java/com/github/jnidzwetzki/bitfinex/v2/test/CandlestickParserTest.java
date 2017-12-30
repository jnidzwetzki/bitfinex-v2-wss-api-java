package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.TickerManager;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;


public class CandlestickParserTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the parsing of one candlestick
	 * @throws APIException
	 */
	@Test
	public void testCandlestickUpdateAndNotify() throws APIException {
		
		final String callbackValue = "[1512684900000,15996,15997,16000,15980,38.51394454]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		final String symbol = "tUSDBTC";
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final TickerManager tickerManager = new TickerManager(executorService);
		
		tickerManager.registerTickCallback(symbol, (s, c) -> {
			Assert.assertEquals(symbol, s);
			Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
			Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
			Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
			Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
			Assert.assertEquals(38.51394454, c.getVolume().toDouble(), DELTA);
		});
		
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getTickerManager()).thenReturn(tickerManager);
		
		final CandlestickHandler candlestickHandler = new CandlestickHandler();
		candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
	}
}
