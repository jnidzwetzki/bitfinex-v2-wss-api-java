package com.github.jnidzwetzki.bitfinex.v2.command;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

/**
 * bitfinex commands factorry
 */
public final class BitfinexCommands {

    private BitfinexCommands() {

    }

    private static OrderCommand newOrder(BitfinexNewOrder order) {
        return new OrderCommand(order);
    }

    private static CancelOrderCommand cancelOrder(long orderId) {
        return new CancelOrderCommand(orderId);
    }

    private static CancelOrderGroupCommand cancelOrderGroup(int orderGroupId) {
        return new CancelOrderGroupCommand(orderGroupId);
    }

    private static PingCommand ping() {
        return new PingCommand();
    }

    private static SubscribeCandlesCommand subscribeCandlesChannel(BitfinexCandlestickSymbol symbol) {
        return new SubscribeCandlesCommand(symbol);
    }

    private static SubscribeOrderbookCommand subscribeOrderbookChannel(BitfinexOrderBookSymbol symbol) {
        return new SubscribeOrderbookCommand(symbol);
    }

    private static SubscribeTickerCommand subscribeTickerChannel(BitfinexTickerSymbol symbol) {
        return new SubscribeTickerCommand(symbol);
    }

    private static SubscribeTradesCommand subscribeTradesChannel(BitfinexExecutedTradeSymbol symbol) {
        return new SubscribeTradesCommand(symbol);
    }

    private static UnsubscribeChannelCommand unsubscribeChannel(BitfinexStreamSymbol symbol) {
        return new UnsubscribeChannelCommand(symbol);
    }

}
