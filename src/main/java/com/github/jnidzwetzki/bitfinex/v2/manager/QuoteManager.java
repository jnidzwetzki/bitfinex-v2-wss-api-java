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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;

public class QuoteManager {

	/**
	 * The last ticks
	 */
	protected final Map<BitfinexStreamSymbol, BitfinexCandle> lastCandle;

	/**
	 * The last tick timestamp
	 */
	protected final Map<BitfinexStreamSymbol, Long> lastTickTimestamp;

	/**
	 * The BitfinexCurrencyPair callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexTickerSymbol, BitfinexCandle> tickerCallbacks;

	/**
	 * The Bitfinex Candlestick callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexCandlestickSymbol, BitfinexCandle> candleCallbacks;

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexExecutedTradeSymbol, ExecutedTrade> tradesCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The bitfinex API
	 */
	private final BitfinexApiBroker bitfinexApiBroker;

	public QuoteManager(final BitfinexApiBroker bitfinexApiBroker) {
		this.bitfinexApiBroker = bitfinexApiBroker;
		this.executorService = bitfinexApiBroker.getExecutorService();
		this.lastCandle = new HashMap<>();
		this.lastTickTimestamp = new HashMap<>();
		this.tickerCallbacks = new BiConsumerCallbackManager<>(executorService);
		this.candleCallbacks = new BiConsumerCallbackManager<>(executorService);
		this.tradesCallbacks = new BiConsumerCallbackManager<>(executorService);
	}

	/**
	 * Get the last heartbeat for the symbol
	 * @param symbol
	 * @return
	 */
	public long getHeartbeatForSymbol(final BitfinexStreamSymbol symbol) {
		synchronized (lastCandle) {
			final Long heartbeat = lastTickTimestamp.get(symbol);

			if(heartbeat == null) {
				return -1;
			}

			return heartbeat;
		}
	}

	/**
	 * Update the channel heartbeat
	 * @param channel
	 */
	public void updateChannelHeartbeat(final BitfinexStreamSymbol symbol) {
		synchronized (lastCandle) {
			lastTickTimestamp.put(symbol, System.currentTimeMillis());
		}
	}

	/**
	 * Get a set with active symbols
	 * @return
	 */
	public Set<BitfinexStreamSymbol> getActiveSymbols() {
		synchronized (lastCandle) {
			return lastCandle.keySet();
		}
	}

	/**
	 * Get the last candle for a given symbol
	 * @param currencyPair
	 * @return
	 */
	public BitfinexCandle getLastCandle(final BitfinexTickerSymbol currencyPair) {
		synchronized (lastCandle) {
			return lastCandle.get(currencyPair);
		}
	}

	/**
	 * Invalidate the ticket heartbeat values
	 */
	public void invalidateTickerHeartbeat() {
		// Invalidate last tick timestamps
		synchronized (lastCandle) {
			lastTickTimestamp.clear();
		}
	}

	/**
	 * Register a new tick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTickCallback(final BitfinexTickerSymbol symbol,
			final BiConsumer<BitfinexTickerSymbol, BitfinexCandle> callback) throws APIException {

		tickerCallbacks.registerCallback(symbol, callback);
	}

	/**
	 * Remove the a tick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTickCallback(final BitfinexTickerSymbol symbol,
			final BiConsumer<BitfinexTickerSymbol, BitfinexCandle> callback) throws APIException {

		return tickerCallbacks.removeCallback(symbol, callback);
	}

	/**
	 * Process a list with candles
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleCandleList(final BitfinexTickerSymbol symbol, final List<BitfinexCandle> candles) {
		tickerCallbacks.handleEventsList(symbol, candles);
	}

	/**
	 * Handle a new candle
	 * @param symbol
	 * @param candle
	 */
	public void handleNewCandle(final BitfinexTickerSymbol currencyPair, final BitfinexCandle candle) {

		synchronized (lastCandle) {
			lastCandle.put(currencyPair, candle);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}

		tickerCallbacks.handleEvent(currencyPair, candle);
	}

	/**
	 * Subscribe a ticker
	 * @param tickerSymbol
	 */
	public void subscribeTicker(final BitfinexTickerSymbol tickerSymbol) {
		final SubscribeTickerCommand command = new SubscribeTickerCommand(tickerSymbol);
		bitfinexApiBroker.sendCommand(command);
	}

	/**
	 * Unsubscribe a ticker
	 * @param tickerSymbol
	 */
	public void unsubscribeTicker(final BitfinexTickerSymbol tickerSymbol) {
		final int channel = bitfinexApiBroker.getChannelForSymbol(tickerSymbol);

		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + tickerSymbol);
		}

		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(tickerSymbol);
	}

	/**
	 * Register a new candlestick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerCandlestickCallback(final BitfinexCandlestickSymbol symbol,
			final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback) throws APIException {

		candleCallbacks.registerCallback(symbol, callback);
	}

	/**
	 * Remove the a candlestick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeCandlestickCallback(final BitfinexCandlestickSymbol symbol,
			final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback) throws APIException {

		return candleCallbacks.removeCallback(symbol, callback);
	}


	/**
	 * Process a list with candlesticks
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleCandlestickList(final BitfinexCandlestickSymbol symbol, final List<BitfinexCandle> ticksBuffer) {
		candleCallbacks.handleEventsList(symbol, ticksBuffer);
	}

	/**
	 * Handle a new candlestick
	 * @param symbol
	 * @param tick
	 */
	public void handleNewCandlestick(final BitfinexCandlestickSymbol currencyPair, final BitfinexCandle tick) {

		synchronized (lastCandle) {
			lastCandle.put(currencyPair, tick);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}

		candleCallbacks.handleEvent(currencyPair, tick);
	}

	/**
	 * Subscribe candles for a symbol
	 * @param currencyPair
	 * @param timeframe
	 */
	public void subscribeCandles(final BitfinexCandlestickSymbol symbol) {
		final SubscribeCandlesCommand command = new SubscribeCandlesCommand(symbol);
		bitfinexApiBroker.sendCommand(command);
	}

	/**
	 * Unsubscribe the candles
	 * @param currencyPair
	 * @param timeframe
	 */
	public void unsubscribeCandles(final BitfinexCandlestickSymbol symbol) {

		final int channel = bitfinexApiBroker.getChannelForSymbol(symbol);

		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + symbol);
		}

		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(symbol);
	}


	/**
	 * Register a new executed trade callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerExecutedTradeCallback(final BitfinexExecutedTradeSymbol orderbookConfiguration,
			final BiConsumer<BitfinexExecutedTradeSymbol, ExecutedTrade> callback) throws APIException {

		tradesCallbacks.registerCallback(orderbookConfiguration, callback);
	}

	/**
	 * Remove a executed trade callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeExecutedTradeCallback(final BitfinexExecutedTradeSymbol tradeSymbol,
			final BiConsumer<BitfinexExecutedTradeSymbol, ExecutedTrade> callback) throws APIException {

		return tradesCallbacks.removeCallback(tradeSymbol, callback);
	}

	/**
	 * Subscribe a executed trade channel
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void subscribeExecutedTrades(final BitfinexExecutedTradeSymbol tradeSymbol) {

		final SubscribeTradesCommand subscribeOrderbookCommand
			= new SubscribeTradesCommand(tradeSymbol);

		bitfinexApiBroker.sendCommand(subscribeOrderbookCommand);
	}

	/**
	 * Unsubscribe a executed trades channel
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 */
	public void unsubscribeExecutedTrades(final BitfinexExecutedTradeSymbol tradeSymbol) {

		final int channel = bitfinexApiBroker.getChannelForSymbol(tradeSymbol);

		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + tradeSymbol);
		}

		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(tradeSymbol);
	}

	/**
	 * Handle a new executed trade
	 * @param symbol
	 * @param tick
	 */
	public void handleExecutedTradeEntry(final BitfinexExecutedTradeSymbol tradeSymbol,
			final ExecutedTrade entry) {

		tradesCallbacks.handleEvent(tradeSymbol, entry);
	}

}
