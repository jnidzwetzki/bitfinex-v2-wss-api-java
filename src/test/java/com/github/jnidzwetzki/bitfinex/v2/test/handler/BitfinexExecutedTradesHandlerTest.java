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

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ExecutedTradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.google.common.util.concurrent.MoreExecutors;

public class BitfinexExecutedTradesHandlerTest {

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
     * Test the parsing of one executed trade
     *
     * @throws BitfinexClientException
     * @throws InterruptedException
     */
    @Test
    public void testExecutedTradesUpdateAndNotify() throws BitfinexClientException, InterruptedException {

        final String callbackValue = "[190631057,1518037080162,0.007,8175.9]";
        final JSONArray jsonArray = new JSONArray(callbackValue);

        final BitfinexExecutedTradeSymbol symbol
                = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BTC", "USD"));

        final ExecutorService executorService = MoreExecutors.newDirectExecutorService();
        final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
        Mockito.doReturn(new BitfinexApiCallbackRegistry()).when(bitfinexApiBroker).getCallbacks();
        final QuoteManager quoteManager = new QuoteManager(bitfinexApiBroker, executorService);
        Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(quoteManager);

        quoteManager.registerExecutedTradeCallback(symbol, (s, c) -> {
            Assert.assertEquals(symbol, s);
            Assert.assertEquals(190631057L, (long) c.getTradeId());
            Assert.assertEquals(1518037080162L, (long) c.getTimestamp());
            Assert.assertEquals(0.007, c.getAmount().doubleValue(), DELTA);
            Assert.assertEquals(8175.9, c.getPrice().doubleValue(), DELTA);
        });

        final ExecutedTradeHandler handler = new ExecutedTradeHandler(10, symbol);
        handler.handleChannelData("te", jsonArray);
    }

    /**
     * Test the parsing of a executed trades snapshot
     *
     * @throws BitfinexClientException
     * @throws InterruptedException
     */
    @Test
    public void testExecutedTradesSnapshotUpdateAndNotify() throws BitfinexClientException, InterruptedException {

        final String callbackValue = "[[190631057,1518037080162,0.007,8175.9],[190631052,1518037080110,-0.25,8175.8]]";
        final JSONArray jsonArray = new JSONArray(callbackValue);

        final BitfinexExecutedTradeSymbol symbol
                = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BTC", "USD"));

        final ExecutorService executorService = MoreExecutors.newDirectExecutorService();
        final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
        Mockito.when(bitfinexApiBroker.getCallbacks()).thenReturn(new BitfinexApiCallbackRegistry());
        final QuoteManager quoteManager = new QuoteManager(bitfinexApiBroker, executorService);
        Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(quoteManager);

        quoteManager.registerExecutedTradeCallback(symbol, (s, c) -> {
            Assert.assertEquals(symbol, s);
            if (c.getTradeId() == 190631057) {
                Assert.assertEquals(1518037080162L, (long) c.getTimestamp());
                Assert.assertEquals(0.007, c.getAmount().doubleValue(), DELTA);
                Assert.assertEquals(8175.9, c.getPrice().doubleValue(), DELTA);
            } else if (c.getTradeId() == 190631052) {
                Assert.assertEquals(1518037080110L, (long) c.getTimestamp());
                Assert.assertEquals(-0.25, c.getAmount().doubleValue(), DELTA);
                Assert.assertEquals(8175.8, c.getPrice().doubleValue(), DELTA);
            } else {
                throw new IllegalArgumentException("Illegal call, expected 2 trades");
            }
        });

        final ExecutedTradeHandler handler = new ExecutedTradeHandler(10, symbol);
        handler.onExecutedTradeEvent((sym, trades) -> {
            trades.forEach(t -> quoteManager.handleExecutedTradeEntry(sym, t));
        });
        handler.handleChannelData("te", jsonArray);
    }

}
