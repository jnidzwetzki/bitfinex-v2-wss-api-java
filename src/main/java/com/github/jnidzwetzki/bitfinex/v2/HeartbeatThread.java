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
package com.github.jnidzwetzki.bitfinex.v2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import org.bboxdb.commons.concurrent.ExceptionSafeRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.command.PingCommand;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.util.EventsInTimeslotManager;

public class HeartbeatThread extends ExceptionSafeRunnable {

	/**
	 * The ticker timeout
	 */
	public static final long TICKER_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	/**
	 * The API timeout
	 */
	public static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(45);

	/**
	 * The API timeout
	 */
	private static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(5);

	/**
	 * Max reconnects in 10 minutes
	 */
	private static final int MAX_RECONNECTS_IN_TIME = 10;

	/**
	 * The API broker
	 */
	private final BitfinexWebsocketClient bitfinexApiBroker;

	/**
	 * websocketEndpoint
	 */
	private final WebsocketClientEndpoint websocketEndpoint;


	/**
	 * The reconnect timeslot manager
	 */
	private final EventsInTimeslotManager eventsInTimeslotManager;

	/**
	 * last heartbeat supplier
	 */
	private final Supplier<Long> lastHeartbeatSupplier;

	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

	/**
	 * callback registry reference
	 */
	private final BitfinexApiCallbackRegistry callbackRegistry;

	/**
	 * new heartbeat thread constructor
	 * @param bitfinexApiBroker			- bitfinex api broken
	 * @param websocketClientEndpoint	- websocket endpoint
	 * @param lastHeartbeatSupplier     - last heartbeat supplier
	 */
	public HeartbeatThread(final BitfinexWebsocketClient bitfinexApiBroker,
						   final WebsocketClientEndpoint websocketClientEndpoint,
						   final Supplier<Long> lastHeartbeatSupplier) {
		this.bitfinexApiBroker = bitfinexApiBroker;
		this.callbackRegistry = (BitfinexApiCallbackRegistry) bitfinexApiBroker.getCallbacks();
		this.websocketEndpoint = websocketClientEndpoint;
		this.lastHeartbeatSupplier = lastHeartbeatSupplier;

		this.eventsInTimeslotManager = new EventsInTimeslotManager(
				MAX_RECONNECTS_IN_TIME,
				10,
				TimeUnit.MINUTES);
	}

    @Override
    public void runThread() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                if (websocketEndpoint == null) {
                    continue;
                }
                if (!websocketEndpoint.isConnected()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("We are not connected, reconnecting");
						executeReconnect();
					}
                    continue;
                }
                sendHeartbeatIfNeeded();
                if (!checkTickerFreshness()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("Ticker are outdated, reconnecting");
						executeReconnect();
					}
                    continue;
                }
                if (checkConnectionTimeout()) {
					callbackRegistry.acceptConnectionStateChange(BitfinexConnectionStateEnum.DISCONNECTION_BY_REMOTE);
					if (this.bitfinexApiBroker.getConfiguration().isAutoReconnect()) {
						logger.error("Global connection heartbeat time out, reconnecting");
						executeReconnect();
					}
                }
            }
        } catch (final InterruptedException e) {
            logger.debug("Heartbeat thread was interrupted, exiting");
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            logger.error("Exception raised", e);
        }
    }

	/**
	 * Are all tickers up-to-date
	 * @return
	 */
	private boolean checkTickerFreshness() {
		final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
		final Map<BitfinexStreamSymbol, Long> heartbeatValues = quoteManager.getLastTickerActivity();

		return checkTickerFreshness(heartbeatValues);
	}

	/**
	 * Are all ticker up-to-date
	 * @return
	 */
	@VisibleForTesting
	public static boolean checkTickerFreshness(final Map<BitfinexStreamSymbol, Long> heartbeatValues) {
		final long currentTime = System.currentTimeMillis();

		final List<BitfinexStreamSymbol> outdatedSymbols = heartbeatValues.entrySet().stream()
			.filter(e -> e.getValue() + TICKER_TIMEOUT < currentTime)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		outdatedSymbols.forEach(symbol -> {
			logger.debug("Symbol {} is outdated by {}ms",
					symbol, currentTime - heartbeatValues.get(symbol));
		});
		return outdatedSymbols.isEmpty();
	}

	/**
	 * Send a heartbeat package on the connection
	 */
	private void sendHeartbeatIfNeeded() {
		final long nextHeartbeat = lastHeartbeatSupplier.get() + HEARTBEAT;

		if(nextHeartbeat < System.currentTimeMillis()) {
			logger.debug("Send heartbeat");
			bitfinexApiBroker.sendCommand(new PingCommand());
		}
	}

	/**
	 * Check for connection timeout
	 * @return
	 */
	private boolean checkConnectionTimeout() {
        return lastHeartbeatSupplier.get() + CONNECTION_TIMEOUT < System.currentTimeMillis();
    }

	/**
	 * Execute the reconnect
	 * @throws InterruptedException
	 */
	private void executeReconnect() throws InterruptedException {
		// Close connection
		websocketEndpoint.close();

		// Store the reconnect time to prevent too much
		// reconnects in a short timeframe. Otherwise the
		// rate limit will apply and the reconnects are not successfully
		logger.info("Wait for next reconnect timeslot");
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.waitForNewTimeslot();
		logger.info("Wait for next reconnect timeslot DONE");

		bitfinexApiBroker.reconnect();
	}
}
