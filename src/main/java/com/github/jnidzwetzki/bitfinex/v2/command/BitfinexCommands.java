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

    public static OrderCommand newOrder(BitfinexNewOrder order) {
        return new OrderCommand(order);
    }

    public static CancelOrderCommand cancelOrder(long orderId) {
        return new CancelOrderCommand(orderId);
    }

    public static CancelOrderGroupCommand cancelOrderGroup(int orderGroupId) {
        return new CancelOrderGroupCommand(orderGroupId);
    }

    public static PingCommand ping() {
        return new PingCommand();
    }

    public static SubscribeCandlesCommand subscribeCandlesChannel(BitfinexCandlestickSymbol symbol) {
        return new SubscribeCandlesCommand(symbol);
    }

    public static SubscribeOrderbookCommand subscribeOrderbookChannel(BitfinexOrderBookSymbol symbol) {
        return new SubscribeOrderbookCommand(symbol);
    }

    public static SubscribeTickerCommand subscribeTickerChannel(BitfinexTickerSymbol symbol) {
        return new SubscribeTickerCommand(symbol);
    }

    public static SubscribeTradesCommand subscribeTradesChannel(BitfinexExecutedTradeSymbol symbol) {
        return new SubscribeTradesCommand(symbol);
    }

    public static UnsubscribeChannelCommand unsubscribeChannel(BitfinexStreamSymbol symbol) {
        return new UnsubscribeChannelCommand(symbol);
    }

}
