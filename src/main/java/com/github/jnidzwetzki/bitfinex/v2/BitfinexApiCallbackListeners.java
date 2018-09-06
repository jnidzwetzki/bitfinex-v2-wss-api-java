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
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.OrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.entity.RawOrderbookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.Trade;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexTickerSymbol;

public class BitfinexApiCallbackListeners {

    protected final Queue<Consumer<BitfinexStreamSymbol>> subscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<BitfinexStreamSymbol>> unsubscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<ExchangeOrder>> exchangeOrderConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<Collection<ExchangeOrder>>> exchangeOrdersConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<Collection<Position>>> positionConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<Trade>> tradeConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<Collection<Wallet>>> walletConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>>> candlesConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>>> executedTradesConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<OrderbookConfiguration, Collection<OrderbookEntry>>> orderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>>> rawOrderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<BiConsumer<BitfinexTickerSymbol, BitfinexTick>> tickConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<ConnectionCapabilities>> authSuccessConsumers = new ConcurrentLinkedQueue<>();
    protected final Queue<Consumer<ConnectionCapabilities>> authFailedConsumers = new ConcurrentLinkedQueue<>();

    public Closeable onSubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        subscribeChannelConsumers.offer(consumer);
        return () -> subscribeChannelConsumers.remove(consumer);
    }

    public Closeable onUnsubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        unsubscribeChannelConsumers.offer(consumer);
        return () -> unsubscribeChannelConsumers.remove(consumer);
    }

    public Closeable onExchangeOrderNotification(final Consumer<ExchangeOrder> consumer) {
        exchangeOrderConsumers.offer(consumer);
        return () -> exchangeOrderConsumers.remove(consumer);
    }

    public Closeable onExchangeOrdersEvent(final Consumer<Collection<ExchangeOrder>> consumer) {
        exchangeOrdersConsumers.offer(consumer);
        return () -> exchangeOrdersConsumers.remove(consumer);
    }

    public Closeable onPositionsEvent(final Consumer<Collection<Position>> consumer) {
        positionConsumers.offer(consumer);
        return () -> positionConsumers.remove(consumer);
    }

    public Closeable onTradeEvent(final Consumer<Trade> consumer) {
        tradeConsumers.offer(consumer);
        return () -> tradeConsumers.remove(consumer);
    }

    public Closeable onWalletsEvent(final Consumer<Collection<Wallet>> consumer) {
        walletConsumers.offer(consumer);
        return () -> walletConsumers.remove(consumer);
    }

    public Closeable onCandlesticksEvent(final BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> consumer) {
        candlesConsumers.offer(consumer);
        return () -> candlesConsumers.remove(consumer);
    }

    public Closeable onExecutedTradeEvent(final BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>> consumer) {
        executedTradesConsumers.offer(consumer);
        return () -> executedTradesConsumers.remove(consumer);
    }

    public Closeable onOrderbookEvent(final BiConsumer<OrderbookConfiguration, Collection<OrderbookEntry>> consumer) {
        orderbookEntryConsumers.offer(consumer);
        return () -> orderbookEntryConsumers.remove(consumer);
    }

    public Closeable onRawOrderbookEvent(final BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>> consumer) {
        rawOrderbookEntryConsumers.offer(consumer);
        return () -> rawOrderbookEntryConsumers.remove(consumer);
    }

    public Closeable onTickEvent(final BiConsumer<BitfinexTickerSymbol, BitfinexTick> consumer) {
        tickConsumers.offer(consumer);
        return () -> tickConsumers.remove(consumer);
    }

    public Closeable onAuthenticationSuccessEvent(final Consumer<ConnectionCapabilities> consumer) {
        authSuccessConsumers.offer(consumer);
        return () -> authSuccessConsumers.remove(consumer);
    }

    public Closeable onAuthenticationFailedEvent(final Consumer<ConnectionCapabilities> consumer) {
        authFailedConsumers.offer(consumer);
        return () -> authFailedConsumers.remove(consumer);
    }

}
