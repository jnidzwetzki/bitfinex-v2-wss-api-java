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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.FutureOperation;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class QuoteManager extends AbstractManager {

	/**
	 * The last ticks
	 */
	private final Map<BitfinexStreamSymbol, Long> lastTickerActivity;

	/**
	 * The BitfinexCurrencyPair callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexTickerSymbol, BitfinexTick> tickerCallbacks;

	/**
	 * The Bitfinex Candlestick callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexCandlestickSymbol, BitfinexCandle> candleCallbacks;

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<BitfinexExecutedTradeSymbol, BitfinexExecutedTrade> tradesCallbacks;

	/**
	 * The pending subscribes
	 */
	private final FutureOperationRegistry pendingSubscribes;
	
	/**
	 * The pending unsubscibes
	 */
	private final FutureOperationRegistry pendingUnsubscribes;
	
	/**
	 * The bitfinex API
	 */
	private final BitfinexWebsocketClient client;
	
	public QuoteManager(final BitfinexWebsocketClient client, final ExecutorService executorService) {
		super(client, executorService);
		this.client = client;
		this.lastTickerActivity = new ConcurrentHashMap<>();
		this.tickerCallbacks = new BiConsumerCallbackManager<>(executorService, client);
		this.candleCallbacks = new BiConsumerCallbackManager<>(executorService, client);
		this.tradesCallbacks = new BiConsumerCallbackManager<>(executorService, client);
		this.pendingSubscribes = new FutureOperationRegistry();
		this.pendingUnsubscribes = new FutureOperationRegistry();
		
		client.getCallbacks().onCandlesticksEvent(this::handleCandlestickCollection);
		client.getCallbacks().onTickEvent(this::handleNewTick);
		client.getCallbacks().onExecutedTradeEvent((sym, trades) -> trades.forEach(t -> this.handleExecutedTradeEntry(sym, t)));
		client.getCallbacks().onSubscribeChannelEvent((s) -> pendingSubscribes.handleEvent(s));
		client.getCallbacks().onUnsubscribeChannelEvent((s) -> pendingUnsubscribes.handleEvent(s));
	}

	/**
	 * Get the last heartbeat for the symbol
	 * @param symbol
	 * @return
	 */
	public long getHeartbeatForSymbol(final BitfinexStreamSymbol symbol) {
		return lastTickerActivity.getOrDefault(symbol, -1L);
	}

	/**
	 * Update the channel heartbeat
	 * @param channel
	 */
	public void updateChannelHeartbeat(final BitfinexStreamSymbol symbol) {
		lastTickerActivity.put(symbol, System.currentTimeMillis());
	}

	/**
	 * Get the last ticker activity
	 * @return
	 */
	public Map<BitfinexStreamSymbol, Long> getLastTickerActivity() {
		return lastTickerActivity;
	}

	/**
	 * Invalidate the ticker heartbeat values
	 */
	public void invalidateTickerHeartbeat() {
		lastTickerActivity.clear();
	}

	/**
	 * Register a new tick callback
	 * @param symbol
	 * @param callback
	 * @throws BitfinexClientException
	 */
	public void registerTickCallback(final BitfinexTickerSymbol symbol,
			final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback) throws BitfinexClientException {

		tickerCallbacks.registerCallback(symbol, callback);
	}

	/**
	 * Remove the a tick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws BitfinexClientException
	 */
	public boolean removeTickCallback(final BitfinexTickerSymbol symbol,
			final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback) throws BitfinexClientException {

		return tickerCallbacks.removeCallback(symbol, callback);
	}

	/**
	 * Process a list with candles
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleCandleCollection(final BitfinexTickerSymbol symbol, final List<BitfinexTick> candles) {
		updateChannelHeartbeat(symbol);
		tickerCallbacks.handleEventsCollection(symbol, candles);
	}

	/**
	 * Handle a new candle
	 * @param symbol
	 * @param candle
	 */
	public void handleNewTick(final BitfinexTickerSymbol currencyPair, final BitfinexTick tick) {
		updateChannelHeartbeat(currencyPair);
		tickerCallbacks.handleEvent(currencyPair, tick);
	}

	/**
	 * Subscribe a ticker
	 * @param tickerSymbol
	 * @return 
	 * @throws BitfinexClientException
	 */
	public FutureOperation subscribeTicker(final BitfinexTickerSymbol tickerSymbol) 
			throws BitfinexClientException {
		
		final FutureOperation future = new FutureOperation(tickerSymbol);
		pendingSubscribes.registerFuture(future);
		
		final SubscribeTickerCommand command = new SubscribeTickerCommand(tickerSymbol);
		client.sendCommand(command);
		
		return future;
	}

	/**
	 * Unsubscribe a ticker
	 * @param tickerSymbol
	 * @return 
	 */
	public FutureOperation unsubscribeTicker(final BitfinexTickerSymbol tickerSymbol) {
		
		final FutureOperation future = new FutureOperation(tickerSymbol);
		pendingUnsubscribes.registerFuture(future);

		lastTickerActivity.remove(tickerSymbol);
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(tickerSymbol);
		client.sendCommand(command);
		
		return future;
	}

	/**
	 * Register a new candlestick callback
	 * @param symbol
	 * @param callback
	 * @throws BitfinexClientException
	 */
	public void registerCandlestickCallback(final BitfinexCandlestickSymbol symbol,
			final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback) throws BitfinexClientException {

		candleCallbacks.registerCallback(symbol, callback);
	}

	/**
	 * Remove the a candlestick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws BitfinexClientException
	 */
	public boolean removeCandlestickCallback(final BitfinexCandlestickSymbol symbol,
			final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback) throws BitfinexClientException {

		return candleCallbacks.removeCallback(symbol, callback);
	}


	/**
	 * Process a list with candlesticks
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleCandlestickCollection(final BitfinexCandlestickSymbol symbol, final Collection<BitfinexCandle> ticksBuffer) {
		candleCallbacks.handleEventsCollection(symbol, ticksBuffer);
	}

	/**
	 * Handle a new candlestick
	 * @param symbol
	 * @param tick
	 */
	public void handleNewCandlestick(final BitfinexCandlestickSymbol currencyPair, final BitfinexCandle tick) {
		updateChannelHeartbeat(currencyPair);
		candleCallbacks.handleEvent(currencyPair, tick);
	}

	/**
	 * Subscribe candles for a symbol
	 * @param currencyPair
	 * @param timeframe
	 * @return 
	 * @throws BitfinexClientException
	 */
	public FutureOperation subscribeCandles(final BitfinexCandlestickSymbol symbol) throws BitfinexClientException {
		
		final FutureOperation future = new FutureOperation(symbol);
		pendingSubscribes.registerFuture(future);
		
		final SubscribeCandlesCommand command = new SubscribeCandlesCommand(symbol);
		client.sendCommand(command);
		
		return future;
	}

	/**
	 * Unsubscribe the candles
	 * @param currencyPair
	 * @param timeframe
	 * @return 
	 */
	public FutureOperation unsubscribeCandles(final BitfinexCandlestickSymbol symbol) {
		lastTickerActivity.remove(symbol);
		
		final FutureOperation future = new FutureOperation(symbol);
		pendingUnsubscribes.registerFuture(future);
		
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(symbol);
		client.sendCommand(command);
		return future;
	}


	/**
	 * Register a new executed trade callback
	 * @param symbol
	 * @param callback
	 * @throws BitfinexClientException
	 */
	public void registerExecutedTradeCallback(final BitfinexExecutedTradeSymbol orderbookConfiguration,
			final BiConsumer<BitfinexExecutedTradeSymbol, BitfinexExecutedTrade> callback) throws BitfinexClientException {

		tradesCallbacks.registerCallback(orderbookConfiguration, callback);
	}

	/**
	 * Remove a executed trade callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws BitfinexClientException
	 */
	public boolean removeExecutedTradeCallback(final BitfinexExecutedTradeSymbol tradeSymbol,
			final BiConsumer<BitfinexExecutedTradeSymbol, BitfinexExecutedTrade> callback) throws BitfinexClientException {

		return tradesCallbacks.removeCallback(tradeSymbol, callback);
	}

	/**
	 * Subscribe a executed trade channel
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 * @return 
	 */
	public FutureOperation subscribeExecutedTrades(final BitfinexExecutedTradeSymbol tradeSymbol) {

		final FutureOperation future = new FutureOperation(tradeSymbol);
		pendingSubscribes.registerFuture(future);
		
		final SubscribeTradesCommand subscribeOrderbookCommand
			= new SubscribeTradesCommand(tradeSymbol);
		
		client.sendCommand(subscribeOrderbookCommand);
		
		return future;
	}

	/**
	 * Unsubscribe a executed trades channel
	 * @param currencyPair
	 * @param orderBookPrecision
	 * @param orderBookFrequency
	 * @param pricePoints
	 * @return 
	 */
	public FutureOperation unsubscribeExecutedTrades(final BitfinexExecutedTradeSymbol tradeSymbol) {
		final FutureOperation future = new FutureOperation(tradeSymbol);
		pendingUnsubscribes.registerFuture(future);

		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(tradeSymbol);
		client.sendCommand(command);
		
		return future;
	}

	/**
	 * Handle a new executed trade
	 * @param symbol
	 * @param tick
	 */
	public void handleExecutedTradeEntry(final BitfinexExecutedTradeSymbol tradeSymbol,
			final BitfinexExecutedTrade entry) {

		tradesCallbacks.handleEvent(tradeSymbol, entry);
	}

}
