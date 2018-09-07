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

import java.util.Collection;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandle;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexTick;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderBookEntry;
import com.github.jnidzwetzki.bitfinex.v2.entity.Position;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public final class BitfinexApiCallbackRegistry extends BitfinexApiCallbackListeners {

    public void acceptSubscribeChannelEvent(final BitfinexStreamSymbol event) {
        subscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptUnsubscribeChannelEvent(final BitfinexStreamSymbol event) {
        unsubscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptExchangeOrderNotification(final ExchangeOrder event) {
        exchangeOrderConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptExchangeOrdersEvent(final Collection<ExchangeOrder> event) {
        exchangeOrdersConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptPositionsEvent(final Collection<Position> event) {
        positionConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptTradeEvent(final BitfinexMyExecutedTrade event) {
        tradeConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptWalletsEvent(final Collection<Wallet> event) {
        walletConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptCandlesticksEvent(final BitfinexCandlestickSymbol symbol, final Collection<BitfinexCandle> entries) {
        candlesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptExecutedTradeEvent(final BitfinexExecutedTradeSymbol symbol, final Collection<BitfinexExecutedTrade> entries) {
        executedTradesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptOrderbookEvent(final BitfinexOrderBookSymbol symbol, final Collection<BitfinexOrderBookEntry> entries) {
        orderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptRawOrderbookEvent(final BitfinexOrderBookSymbol symbol, final Collection<BitfinexOrderBookEntry> entries) {
        rawOrderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptTickEvent(final BitfinexTickerSymbol symbol, final BitfinexTick tick) {
        tickConsumers.forEach(consumer -> consumer.accept(symbol, tick));
    }

    public void acceptAuthenticationSuccessEvent(final BitfinexApiKeyPermissions event) {
        authSuccessConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptAuthenticationFailedEvent(final BitfinexApiKeyPermissions event) {
        authFailedConsumers.forEach(consumer -> consumer.accept(event));
    }

}
