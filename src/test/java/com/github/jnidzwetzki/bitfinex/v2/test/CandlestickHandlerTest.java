package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.TickerManager;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;


public class CandlestickHandlerTest {

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
		
		final String callbackValue = "[15134900000,15996,15997,16000,15980,318.5139342]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		final String symbol = "tUSDBTC";
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final TickerManager tickerManager = new TickerManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getTickerManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);

		tickerManager.registerTickCallback(symbol, (s, c) -> {
			counter.incrementAndGet();
			Assert.assertEquals(symbol, s);
			Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
			Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
			Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
			Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
			Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler();
		candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		Assert.assertEquals(1, counter.get());
	}
	
	
	/**
	 * Test the parsing of a candlestick snapshot
	 * @throws APIException
	 */
	@Test
	public void testCandlestickSnapshotUpdateAndNotify() throws APIException {
		
		final String callbackValue = "[[15134900000,15996,15997,16000,15980,318.5139342],[15135100000,15899,15996,16097,15890,1137.180342268]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		final String symbol = "tUSDBTC";
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		final TickerManager tickerManager = new TickerManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getTickerManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);
		
		tickerManager.registerTickCallback(symbol, (s, c) -> {
			Assert.assertEquals(symbol, s);
			final int counterValue = counter.getAndIncrement();
			if(counterValue == 0) {
				Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
				Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
				Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
				Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
				Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
			} else if(counterValue == 1) {
				Assert.assertEquals(15899, c.getOpenPrice().toDouble(), DELTA);
				Assert.assertEquals(15996, c.getClosePrice().toDouble(), DELTA);
				Assert.assertEquals(16097, c.getMaxPrice().toDouble(), DELTA);
				Assert.assertEquals(15890, c.getMinPrice().toDouble(), DELTA);
				Assert.assertEquals(1137.180342268, c.getVolume().toDouble(), DELTA);
			} else {
				throw new IllegalArgumentException("Illegal call, expected 2 candlesticks");
			}
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler();
		candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);
		
		Assert.assertEquals(2, counter.get());
	}
		
}
