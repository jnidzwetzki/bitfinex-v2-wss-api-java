package com.github.jnidzwetzki.bitfinex.v2.test.integration;

import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexClientFactory;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.PooledBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;

public class PooledBitfinexApiBrokerIT {

    @BeforeClass
    public static void registerDefaultCurrencyPairs() {
        BitfinexCurrencyPair.registerDefaults();
    }

    @AfterClass
    public static void unregisterDefaultCurrencyPairs() {
        BitfinexCurrencyPair.unregisterAll();
    }

    @Test(timeout = 60_000)
    public void testSubscriptions() throws InterruptedException {
        // given
        BitfinexWebsocketConfiguration config = new BitfinexWebsocketConfiguration();
        PooledBitfinexApiBroker client = (PooledBitfinexApiBroker) BitfinexClientFactory.newPooledClient(config, 50);

        int channelLimit = 150;
        // when
        CountDownLatch subsLatch = new CountDownLatch(channelLimit * 4);
        client.getCallbacks().onSubscribeChannelEvent(chan -> subsLatch.countDown());

        client.connect();
        BitfinexCurrencyPair.values().stream()
                .limit(channelLimit)
                .forEach(bfxPair -> {
                    client.sendCommand(new SubscribeCandlesCommand(BitfinexSymbols.candlesticks(bfxPair, BitfinexCandleTimeFrame.MINUTES_1)));
                    client.sendCommand(new SubscribeOrderbookCommand(BitfinexSymbols.orderBook(bfxPair, BitfinexOrderBookSymbol.Precision.P0, BitfinexOrderBookSymbol.Frequency.F0, 100)));
                    client.sendCommand(new SubscribeTickerCommand(BitfinexSymbols.ticker(bfxPair)));
                    client.sendCommand(new SubscribeTradesCommand(BitfinexSymbols.executedTrades(bfxPair)));
                });
        // then
        subsLatch.await();
        Assert.assertEquals(channelLimit * 4, client.getSubscribedChannels().size());
        Assert.assertEquals(3, client.websocketConnCount());
    }

}
