package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import org.ta4j.core.Tick;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;

public class QuoteManager {
	
	/**
	 * The last ticks
	 */
	protected final Map<BitfinexStreamSymbol, Tick> lastTick;
	
	/**
	 * The last tick timestamp
	 */
	protected final Map<BitfinexStreamSymbol, Long> lastTickTimestamp;
	
	/**
	 * The BitfinexCurrencyPair callbacks
	 */
	private final Map<BitfinexCurrencyPair, List<BiConsumer<BitfinexCurrencyPair, Tick>>> tickerCallbacks;

	/**
	 * The Bitfinex Candlestick callbacks
	 */
	private final Map<BitfinexCandlestickSymbol, List<BiConsumer<BitfinexCandlestickSymbol, Tick>>> candleCallbacks;
	
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
		this.lastTick = new HashMap<>();
		this.lastTickTimestamp = new HashMap<>();
		this.tickerCallbacks = new HashMap<>();
		this.candleCallbacks = new HashMap<>();
	}
	
	/**
	 * Get the last heartbeat for the symbol
	 * @param symbol
	 * @return
	 */
	public long getHeartbeatForSymbol(final BitfinexStreamSymbol symbol) {
		synchronized (lastTick) {
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
		synchronized (lastTick) {
			lastTickTimestamp.put(symbol, System.currentTimeMillis());
		}
	}
	
	/**
	 * Get a set with active symbols
	 * @return
	 */
	public Set<BitfinexStreamSymbol> getActiveSymbols() {
		synchronized (lastTick) {
			return lastTick.keySet();
		}
	}
	
	/**
	 * Get the last tick for a given symbol
	 * @param currencyPair
	 * @return 
	 */
	public Tick getLastTick(final BitfinexCurrencyPair currencyPair) {
		synchronized (lastTick) {
			return lastTick.get(currencyPair);
		}
	}
	
	/**
	 * Invalidate the ticket heartbeat values
	 */
	public void invalidateTickerHeartbeat() {
		// Invalidate last tick timetamps
		synchronized (lastTick) {
			lastTickTimestamp.clear();	
		}
	}
	
	/**
	 * Register a new tick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTickCallback(final BitfinexCurrencyPair symbol, 
			final BiConsumer<BitfinexCurrencyPair, Tick> callback) throws APIException {
		
		tickerCallbacks.putIfAbsent(symbol, new ArrayList<>());
				
		final List<BiConsumer<BitfinexCurrencyPair, Tick>> callbacks = tickerCallbacks.get(symbol);
		
		synchronized (callbacks) {
			callbacks.add(callback);	
		}
	}
	
	/**
	 * Remove the a tick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTickCallback(final BitfinexCurrencyPair symbol, 
			final BiConsumer<BitfinexCurrencyPair, Tick> callback) throws APIException {
		
		if(! tickerCallbacks.containsKey(symbol)) {
			throw new APIException("Unknown ticker string: " + symbol);
		}
			
		final List<BiConsumer<BitfinexCurrencyPair, Tick>> callbacks = tickerCallbacks.get(symbol);
		
		synchronized (callbacks) {
			return callbacks.remove(callback);
		}
	}
	
	/**
	 * Process a list with ticks
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleTicksList(final BitfinexCurrencyPair symbol, final List<Tick> ticksBuffer) {
		
		// Notify callbacks async
		final List<BiConsumer<BitfinexCurrencyPair, Tick>> callbacks = tickerCallbacks.get(symbol);
		
		if(callbacks == null) {
			return;
		}
				
		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}
			
			for (final Tick tick : ticksBuffer) {
				callbacks.forEach((c) -> {
					c.accept(symbol, tick);
				});
			}
		}
	}
	
	/**
	 * Handle a new tick
	 * @param symbol
	 * @param tick
	 */
	public void handleNewTick(final BitfinexCurrencyPair currencyPair, final Tick tick) {
		
		synchronized (lastTick) {
			lastTick.put(currencyPair, tick);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}
		
		final List<BiConsumer<BitfinexCurrencyPair, Tick>> callbacks = tickerCallbacks.get(currencyPair);
		
		if(callbacks == null) {
			return;
		}

		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}

			callbacks.forEach((c) -> {
				final Runnable runnable = () -> c.accept(currencyPair, tick);
				executorService.submit(runnable);
			});
		}
	}
	
	/**
	 * Subscribe a ticker
	 * @param currencyPair
	 */
	public void subscribeTicker(final BitfinexCurrencyPair currencyPair) {
		final SubscribeTickerCommand command = new SubscribeTickerCommand(currencyPair);
		bitfinexApiBroker.sendCommand(command);
	}
	
	/**
	 * Unsubscribe a ticker
	 * @param currencyPair
	 */
	public void unsubscribeTicker(final BitfinexCurrencyPair currencyPair) {		
		final int channel = bitfinexApiBroker.getChannelForSymbol(currencyPair);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + currencyPair);
		}
		
		final UnsubscribeChannelCommand command = new UnsubscribeChannelCommand(channel);
		bitfinexApiBroker.sendCommand(command);
		bitfinexApiBroker.removeChannelForSymbol(currencyPair);
	}
	
	/**
	 * Register a new candlestick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerCandlestickCallback(final BitfinexCandlestickSymbol symbol, 
			final BiConsumer<BitfinexCandlestickSymbol, Tick> callback) throws APIException {
		
		candleCallbacks.putIfAbsent(symbol, new ArrayList<>());
				
		final List<BiConsumer<BitfinexCandlestickSymbol, Tick>> callbacks = candleCallbacks.get(symbol);
		
		synchronized (callbacks) {
			callbacks.add(callback);	
		}
	}
	
	/**
	 * Remove the a candlestick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeCandlestickCallback(final BitfinexCandlestickSymbol symbol, 
			final BiConsumer<BitfinexCandlestickSymbol, Tick> callback) throws APIException {
		
		if(! candleCallbacks.containsKey(symbol)) {
			throw new APIException("Unknown ticker string: " + symbol);
		}
			
		final List<BiConsumer<BitfinexCandlestickSymbol, Tick>> callbacks = candleCallbacks.get(symbol);
		
		synchronized (callbacks) {
			return callbacks.remove(callback);
		}
	}
	

	/**
	 * Process a list with candlesticks
	 * @param symbol
	 * @param ticksArray
	 */
	public void handleCandlestickList(final BitfinexCandlestickSymbol symbol, final List<Tick> ticksBuffer) {
		
		// Notify callbacks async
		final List<BiConsumer<BitfinexCandlestickSymbol, Tick>> callbacks = candleCallbacks.get(symbol);
		
		if(callbacks == null) {
			return;
		}
				
		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}
			
			for (final Tick tick : ticksBuffer) {
				callbacks.forEach((c) -> {
					c.accept(symbol, tick);
				});
			}
		}
	}
	
	/**
	 * Handle a new candlestick
	 * @param symbol
	 * @param tick
	 */
	public void handleNewCandlestick(final BitfinexCandlestickSymbol currencyPair, final Tick tick) {
		
		synchronized (lastTick) {
			lastTick.put(currencyPair, tick);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}
		
		final List<BiConsumer<BitfinexCandlestickSymbol, Tick>> callbacks = candleCallbacks.get(currencyPair);
		
		if(callbacks == null) {
			return;
		}

		synchronized(callbacks) {
			if(callbacks.isEmpty()) {
				return;
			}

			callbacks.forEach((c) -> {
				final Runnable runnable = () -> c.accept(currencyPair, tick);
				executorService.submit(runnable);
			});
		}
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
	
}
