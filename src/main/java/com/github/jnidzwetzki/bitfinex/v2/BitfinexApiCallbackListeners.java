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

    public Closeable onSubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        subscribeChannelConsumers.offer(consumer);
        return () -> subscribeChannelConsumers.remove(consumer);
    }

    public Closeable onUnsubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        unsubscribeChannelConsumers.offer(consumer);
        return () -> unsubscribeChannelConsumers.remove(consumer);
    }

    public Closeable onOrderNotification(final BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> consumer) {
        newOrderConsumers.offer(consumer);
        return () -> newOrderConsumers.remove(consumer);
    }

    public Closeable onSubmittedOrderEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> consumer) {
        submittedOrderConsumers.offer(consumer);
        return () -> submittedOrderConsumers.remove(consumer);
    }

    public Closeable onPositionsEvent(final BiConsumer<BitfinexAccountSymbol,Collection<BitfinexPosition>> consumer) {
        positionConsumers.offer(consumer);
        return () -> positionConsumers.remove(consumer);
    }

    public Closeable onTradeEvent(final BiConsumer<BitfinexAccountSymbol,BitfinexMyExecutedTrade> consumer) {
        tradeConsumers.offer(consumer);
        return () -> tradeConsumers.remove(consumer);
    }

    public Closeable onWalletsEvent(final BiConsumer<BitfinexAccountSymbol,Collection<BitfinexWallet>> consumer) {
        walletConsumers.offer(consumer);
        return () -> walletConsumers.remove(consumer);
    }

    public Closeable onCandlesticksEvent(final BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> consumer) {
        candlesConsumers.offer(consumer);
        return () -> candlesConsumers.remove(consumer);
    }

    public Closeable onExecutedTradeEvent(final BiConsumer<BitfinexExecutedTradeSymbol, Collection<BitfinexExecutedTrade>> consumer) {
        executedTradesConsumers.offer(consumer);
        return () -> executedTradesConsumers.remove(consumer);
    }

    public Closeable onOrderbookEvent(final BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> consumer) {
        orderbookEntryConsumers.offer(consumer);
        return () -> orderbookEntryConsumers.remove(consumer);
    }

    public Closeable onRawOrderbookEvent(final BiConsumer<BitfinexOrderBookSymbol, Collection<BitfinexOrderBookEntry>> consumer) {
        rawOrderbookEntryConsumers.offer(consumer);
        return () -> rawOrderbookEntryConsumers.remove(consumer);
    }

    public Closeable onTickEvent(final BiConsumer<BitfinexTickerSymbol, BitfinexTick> consumer) {
        tickConsumers.offer(consumer);
        return () -> tickConsumers.remove(consumer);
    }

    public Closeable onAuthenticationSuccessEvent(final Consumer<BitfinexAccountSymbol> consumer) {
        authSuccessConsumers.offer(consumer);
        return () -> authSuccessConsumers.remove(consumer);
    }

    public Closeable onAuthenticationFailedEvent(final Consumer<BitfinexAccountSymbol> consumer) {
        authFailedConsumers.offer(consumer);
        return () -> authFailedConsumers.remove(consumer);
    }

}
