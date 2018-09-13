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

public final class BitfinexApiCallbackRegistry extends BitfinexApiCallbackListeners {

    public void acceptSubscribeChannelEvent(final BitfinexStreamSymbol event) {
        subscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptUnsubscribeChannelEvent(final BitfinexStreamSymbol event) {
        unsubscribeChannelConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptMyOrderNotification(final BitfinexAccountSymbol symbol, final BitfinexSubmittedOrder event) {
        newOrderConsumers.forEach(consumer -> consumer.accept(symbol, event));
    }

    public void acceptMySubmittedOrderEvent(final BitfinexAccountSymbol symbol, final Collection<BitfinexSubmittedOrder> event) {
        submittedOrderConsumers.forEach(consumer -> consumer.accept(symbol, event));
    }

    public void acceptMyPositionEvent(final BitfinexAccountSymbol symbol, final Collection<BitfinexPosition> event) {
        positionConsumers.forEach(consumer -> consumer.accept(symbol, event));
    }

    public void acceptMyTradeEvent(final BitfinexAccountSymbol symbol, final BitfinexMyExecutedTrade event) {
        tradeConsumers.forEach(consumer -> consumer.accept(symbol, event));
    }

    public void acceptMyWalletEvent(final BitfinexAccountSymbol symbol, final Collection<BitfinexWallet> event) {
        walletConsumers.forEach(consumer -> consumer.accept(symbol, event));
    }

    public void acceptCandlesticksEvent(final BitfinexCandlestickSymbol symbol, final Collection<BitfinexCandle> entries) {
        candlesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptExecutedTradeEvent(final BitfinexExecutedTradeSymbol symbol, final Collection<BitfinexExecutedTrade> entries) {
        executedTradesConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptOrderBookEvent(final BitfinexOrderBookSymbol symbol, final Collection<BitfinexOrderBookEntry> entries) {
        orderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptRawOrderBookEvent(final BitfinexOrderBookSymbol symbol, final Collection<BitfinexOrderBookEntry> entries) {
        rawOrderbookEntryConsumers.forEach(consumer -> consumer.accept(symbol, entries));
    }

    public void acceptTickEvent(final BitfinexTickerSymbol symbol, final BitfinexTick tick) {
        tickConsumers.forEach(consumer -> consumer.accept(symbol, tick));
    }

    public void acceptAuthenticationSuccessEvent(final BitfinexAccountSymbol event) {
        authSuccessConsumers.forEach(consumer -> consumer.accept(event));
    }

    public void acceptAuthenticationFailedEvent(final BitfinexAccountSymbol event) {
        authFailedConsumers.forEach(consumer -> consumer.accept(event));
    }

}
