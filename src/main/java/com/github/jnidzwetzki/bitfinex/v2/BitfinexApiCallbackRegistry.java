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

public class BitfinexApiCallbackRegistry {

    private final Queue<Consumer<BitfinexStreamSymbol>> subscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<BitfinexStreamSymbol>> unsubscribeChannelConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<ExchangeOrder>> exchangeOrderConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<Collection<ExchangeOrder>>> exchangeOrdersConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<Collection<Position>>> positionConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<Trade>> tradeConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<Collection<Wallet>>> walletConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>>> candlesConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>>> executedTradesConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<BiConsumer<OrderbookConfiguration, Collection<OrderbookEntry>>> orderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>>> rawOrderbookEntryConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<BiConsumer<BitfinexTickerSymbol, BitfinexTick>> tickConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<ConnectionCapabilities>> authSuccessConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<Consumer<ConnectionCapabilities>> authFailedConsumers = new ConcurrentLinkedQueue<>();

    public Closeable onSubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        subscribeChannelConsumers.offer(consumer);
        return () -> subscribeChannelConsumers.remove(consumer);
    }

    public void acceptSubscribeChannelEvent(final BitfinexStreamSymbol event) {
        subscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onUnsubscribeChannelEvent(final Consumer<BitfinexStreamSymbol> consumer) {
        unsubscribeChannelConsumers.offer(consumer);
        return () -> unsubscribeChannelConsumers.remove(consumer);
    }

    public void acceptUnsubscribeChannelEvent(final BitfinexStreamSymbol event) {
        unsubscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onExchangeOrderNotification(final Consumer<ExchangeOrder> consumer) {
        exchangeOrderConsumers.offer(consumer);
        return () -> exchangeOrderConsumers.remove(consumer);
    }

    public void acceptExchangeOrderNotification(final ExchangeOrder event) {
        exchangeOrderConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onExchangeOrdersEvent(final Consumer<Collection<ExchangeOrder>> consumer) {
        exchangeOrdersConsumers.offer(consumer);
        return () -> exchangeOrdersConsumers.remove(consumer);
    }

    public void acceptExchangeOrdersEvent(final Collection<ExchangeOrder> event) {
        exchangeOrdersConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onPositionsEvent(final Consumer<Collection<Position>> consumer) {
        positionConsumers.offer(consumer);
        return () -> positionConsumers.remove(consumer);
    }

    public void acceptPositionsEvent(final Collection<Position> event) {
        positionConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onTradeEvent(final Consumer<Trade> consumer) {
        tradeConsumers.offer(consumer);
        return () -> tradeConsumers.remove(consumer);
    }

    public void acceptTradeEvent(final Trade event) {
        tradeConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onWalletsEvent(final Consumer<Collection<Wallet>> consumer) {
        walletConsumers.offer(consumer);
        return () -> walletConsumers.remove(consumer);
    }

    public void acceptWalletsEvent(final Collection<Wallet> event) {
        walletConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onCandlesticksEvent(final BiConsumer<BitfinexCandlestickSymbol, Collection<BitfinexCandle>> consumer) {
        candlesConsumers.offer(consumer);
        return () -> candlesConsumers.remove(consumer);
    }

    public void acceptCandlesticksEvent(final BitfinexCandlestickSymbol symbol, final Collection<BitfinexCandle> entries) {
        candlesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public Closeable onExecutedTradeEvent(final BiConsumer<BitfinexExecutedTradeSymbol, Collection<ExecutedTrade>> consumer) {
        executedTradesConsumers.offer(consumer);
        return () -> executedTradesConsumers.remove(consumer);
    }

    public void acceptExecutedTradeEvent(final BitfinexExecutedTradeSymbol symbol, final Collection<ExecutedTrade> entries) {
        executedTradesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public Closeable onOrderbookEvent(final BiConsumer<OrderbookConfiguration, Collection<OrderbookEntry>> consumer) {
        orderbookEntryConsumers.offer(consumer);
        return () -> orderbookEntryConsumers.remove(consumer);
    }

    public void acceptOrderbookEvent(final OrderbookConfiguration symbol, final Collection<OrderbookEntry> entries) {
        orderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public Closeable onRawOrderbookEvent(final BiConsumer<RawOrderbookConfiguration, Collection<RawOrderbookEntry>> consumer) {
        rawOrderbookEntryConsumers.offer(consumer);
        return () -> rawOrderbookEntryConsumers.remove(consumer);
    }

    public void acceptRawOrderbookEvent(final RawOrderbookConfiguration symbol, final Collection<RawOrderbookEntry> entries) {
        rawOrderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public Closeable onTickEvent(final BiConsumer<BitfinexTickerSymbol, BitfinexTick> consumer) {
        tickConsumers.offer(consumer);
        return () -> tickConsumers.remove(consumer);
    }

    public void acceptTickEvent(final BitfinexTickerSymbol symbol, final BitfinexTick tick) {
        tickConsumers.forEach(consumer -> consumer.accept(symbol, tick));
    }

    public Closeable onAuthenticationSuccessEvent(final Consumer<ConnectionCapabilities> consumer) {
        authSuccessConsumers.offer(consumer);
        return () -> authSuccessConsumers.remove(consumer);
    }

    public void acceptAuthenticationSuccessEvent(final ConnectionCapabilities event) {
        authSuccessConsumers.forEach(consumer -> consumer.accept(event));
    }

    public Closeable onAuthenticationFailedEvent(final Consumer<ConnectionCapabilities> consumer) {
        authFailedConsumers.offer(consumer);
        return () -> authFailedConsumers.remove(consumer);
    }

    public void acceptAuthenticationFailedEvent(final ConnectionCapabilities event) {
        authFailedConsumers.forEach(consumer -> consumer.accept(event));
    }

}
