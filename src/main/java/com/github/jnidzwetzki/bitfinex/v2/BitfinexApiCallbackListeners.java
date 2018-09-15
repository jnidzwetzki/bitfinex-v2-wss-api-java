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

import java.io.Closeable;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

/**
 * Main registry of events listeners happening within integration with bitfinex exchange
 */
public class BitfinexApiCallbackListeners {

    protected final Queue<Consumer<BitfinexStreamSymbol>> subscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<BitfinexStreamSymbol>> unsubscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder>> newOrderConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>>> submittedOrderConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>>> positionConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade>> tradeConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexAccountSymbol, Collection<BitfinexWallet>>> walletConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>>> candlesConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexExecutedTradeSymbol, Collection<BitfinexExecutedTrade>>> executedTradesConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>>> orderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>>> rawOrderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexTickerSymbol, BitfinexTick>> tickConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<BitfinexAccountSymbol>> authSuccessConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<BitfinexAccountSymbol>> authFailedConsumers = new ConcurrentLinkedQueue<>();

    /**
     * registers listener for subscribe events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onSubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> listener) {
        subscribeChannelConsumers.offer(listener);
        return () -> subscribeChannelConsumers.remove(listener);
    }

    /**
     * registers listener for unsubscribe events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onUnsubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> listener) {
        unsubscribeChannelConsumers.offer(listener);
        return () -> unsubscribeChannelConsumers.remove(listener);
    }

    /**
     * registers listener for my order notifications
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onMyOrderNotification(final BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> listener) {
        newOrderConsumers.offer(listener);
        return () -> newOrderConsumers.remove(listener);
    }

    /**
     * registers listener for user account related events - submitted order events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onMySubmittedOrderEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> listener) {
        submittedOrderConsumers.offer(listener);
        return () -> submittedOrderConsumers.remove(listener);
    }

    /**
     * registers listener for user account related events - position events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onMyPositionEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>> listener) {
        positionConsumers.offer(listener);
        return () -> positionConsumers.remove(listener);
    }

    /**
     * registers listener for user account related events - executed trades (against submitted order) events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onMyTradeEvent(final BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade> listener) {
        tradeConsumers.offer(listener);
        return () -> tradeConsumers.remove(listener);
    }

    /**
     * registers listener for user account related events - wallet change events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onMyWalletEvent(final BiConsumer<BitfinexAccountSymbol,Collection<BitfinexWallet>> listener) {
        walletConsumers.offer(listener);
        return () -> walletConsumers.remove(listener);
    }

    /**
     * registers listener for candlesticks info updates
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onCandlesticksEvent(final BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> listener) {
        candlesConsumers.offer(listener);
        return () -> candlesConsumers.remove(listener);
    }

    /**
     * registers listener for general trades executed within scope of exchange instrument (ie. tBTCUSD)
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onExecutedTradeEvent(final BiConsumer<BitfinexExecutedTradeSymbol, Collection<BitfinexExecutedTrade>> listener) {
        executedTradesConsumers.offer(listener);
        return () -> executedTradesConsumers.remove(listener);
    }

    /**
     * registers listener for orderbook events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onOrderbookEvent(final BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> listener) {
        orderbookEntryConsumers.offer(listener);
        return () -> orderbookEntryConsumers.remove(listener);
    }

    /**
     * registers listener for raw orderbook events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onRawOrderbookEvent(final BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> listener) {
        rawOrderbookEntryConsumers.offer(listener);
        return () -> rawOrderbookEntryConsumers.remove(listener);
    }

    /**
     * registers listener for tick events
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onTickEvent(final BiConsumer<BitfinexTickerSymbol, BitfinexTick> listener) {
        tickConsumers.offer(listener);
        return () -> tickConsumers.remove(listener);
    }

    /**
     * registers listener for event of successful authentication with api-key
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onAuthenticationSuccessEvent(final Consumer<BitfinexAccountSymbol> listener) {
        authSuccessConsumers.offer(listener);
        return () -> authSuccessConsumers.remove(listener);
    }

    /**
     * registers listener for event of failed authentication with api-key
     * @param listener of event
     * @return hook of this listener
     */
    public Closeable onAuthenticationFailedEvent(final Consumer<BitfinexAccountSymbol> listener) {
        authFailedConsumers.offer(listener);
        return () -> authFailedConsumers.remove(listener);
    }

}
