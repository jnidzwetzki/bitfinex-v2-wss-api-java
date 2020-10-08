package com.github.jnidzwetzki.bitfinex.v2.command;

import java.util.Collection;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

/**
 * bitfinex commands factory
 */
public final class BitfinexCommands {

    private BitfinexCommands() {

    }

    public static OrderNewCommand newOrder(BitfinexOrder order) {
        return new OrderNewCommand(order);
    }

    public static OrderCancelCommand cancelOrder(long orderId) {
        return new OrderCancelCommand(orderId);
    }

    public static OrderCancelGroupCommand cancelOrderGroup(int orderGroupId) {
        return new OrderCancelGroupCommand(orderGroupId);
    }

    public static OrderCancelAllCommand cancelAllOrders() {
        return new OrderCancelAllCommand();
    }

    public static OrderMultiCommand orderMulti(Collection<BitfinexOrderCommand> commands) {
        return new OrderMultiCommand(commands);
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
