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
package com.github.jnidzwetzki.bitfinex.v2.test.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.CandlestickHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;


public class CandlestickHandlerTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the parsing of one candlestick
	 * @throws BitfinexClientException
	 */
	@Test
	public void testCandlestickUpdateAndNotify() throws BitfinexClientException {
		
		final String callbackValue = "[15134900000,15996,15997,16000,15980,318.5139342]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final BitfinexCandlestickSymbol symbol 
			= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MINUTES_1);

		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getCallbacks()).thenReturn(new BitfinexApiCallbackRegistry());

		final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker, executorService);
		Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);

		tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
			counter.incrementAndGet();
			Assert.assertEquals(symbol, s);
			Assert.assertEquals(15996, c.getOpen().doubleValue(), DELTA);
			Assert.assertEquals(15997, c.getClose().doubleValue(), DELTA);
			Assert.assertEquals(16000, c.getHigh().doubleValue(), DELTA);
			Assert.assertEquals(15980, c.getLow().doubleValue(), DELTA);
			Assert.assertEquals(318.5139342, c.getVolume().get().doubleValue(), DELTA);
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler(10, symbol);
		candlestickHandler.onCandlesticksEvent(bitfinexApiBroker.getQuoteManager()::handleCandlestickCollection);
		candlestickHandler.handleChannelData(null, jsonArray);
		
		Assert.assertEquals(1, counter.get());
	}
	
	
	/**
	 * Test the parsing of a candlestick snapshot
	 * @throws BitfinexClientException
	 */
	@Test
	public void testCandlestickSnapshotUpdateAndNotify() throws BitfinexClientException {
		
		final String callbackValue = "[[15134900000,15996,15997,16000,15980,318.5139342],[15135100000,15899,15996,16097,15890,1137.180342268]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final BitfinexCandlestickSymbol symbol 
			= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MINUTES_1);

		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getCallbacks()).thenReturn(new BitfinexApiCallbackRegistry());

		final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker, executorService);
		Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(tickerManager);

		final AtomicInteger counter = new AtomicInteger(0);
		
		tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
			Assert.assertEquals(symbol, s);
			final int counterValue = counter.getAndIncrement();
			if(counterValue == 0) {
				Assert.assertEquals(15996, c.getOpen().doubleValue(), DELTA);
				Assert.assertEquals(15997, c.getClose().doubleValue(), DELTA);
				Assert.assertEquals(16000, c.getHigh().doubleValue(), DELTA);
				Assert.assertEquals(15980, c.getLow().doubleValue(), DELTA);
				Assert.assertEquals(318.5139342, c.getVolume().get().doubleValue(), DELTA);
			} else if(counterValue == 1) {
				Assert.assertEquals(15899, c.getOpen().doubleValue(), DELTA);
				Assert.assertEquals(15996, c.getClose().doubleValue(), DELTA);
				Assert.assertEquals(16097, c.getHigh().doubleValue(), DELTA);
				Assert.assertEquals(15890, c.getLow().doubleValue(), DELTA);
				Assert.assertEquals(1137.180342268, c.getVolume().get().doubleValue(), DELTA);
			} else {
				throw new IllegalArgumentException("Illegal call, expected 2 candlesticks");
			}
		});
						
		final CandlestickHandler candlestickHandler = new CandlestickHandler(10, symbol);
		candlestickHandler.onCandlesticksEvent(bitfinexApiBroker.getQuoteManager()::handleCandlestickCollection);
		candlestickHandler.handleChannelData(null, jsonArray);
		
		Assert.assertEquals(2, counter.get());
	}
	
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test
	public void testCandlestickSymbolEncoding1() {
		final BitfinexCandlestickSymbol symbol1 
			= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), BitfinexCandleTimeFrame.MINUTES_15);
		
		final BitfinexCandlestickSymbol symbol2
			= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("ETH","USD"), BitfinexCandleTimeFrame.MINUTES_15);
	
		final BitfinexCandlestickSymbol symbol3
				= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTCF0","USTF0"), BitfinexCandleTimeFrame.MINUTES_1);
		
		Assert.assertFalse(symbol1.equals(symbol2));
		
		final String symbol1String = symbol1.toBifinexCandlestickString();
		final String symbol2String = symbol2.toBifinexCandlestickString();
		final String symbol3String = symbol3.toBifinexCandlestickString();
		
		Assert.assertEquals(symbol1, BitfinexCandlestickSymbol.fromBitfinexString(symbol1String));
		Assert.assertEquals(symbol2, BitfinexCandlestickSymbol.fromBitfinexString(symbol2String));
		Assert.assertEquals(symbol3, BitfinexCandlestickSymbol.fromBitfinexString(symbol3String));
	}
		
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testCandlestickSymbolEncoding2() {
		final String symbol = "dffdsf:dfsfd:dsdfd";
		BitfinexCandlestickSymbol.fromBitfinexString(symbol);
	}
	
	/**
	 * Test the symbol encoding and decoding
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testCandlestickSymbolEncoding3() {
		final String symbol = "trading:";
		BitfinexCandlestickSymbol.fromBitfinexString(symbol);
	}
}
