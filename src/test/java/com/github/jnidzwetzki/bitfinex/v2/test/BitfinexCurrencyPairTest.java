package com.github.jnidzwetzki.bitfinex.v2.test;

import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexCurrencyPair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

public class BitfinexCurrencyPairTest {

    @BeforeClass
    public static void registerDefaultCurrencyPairs() {
        BitfinexCurrencyPair.unregisterAll();
    }

    /**
     * Test parsing the pairs.
     */
    @Test
    public void registerDefaultsTestSplitting() {
        BitfinexCurrencyPair.registerDefaults();
        Collection<BitfinexCurrencyPair> pairs = BitfinexCurrencyPair.values();
        // pair "btcusd"
        Assert.assertEquals(1,
                pairs.stream().filter(p -> p.getCurrency1().equals("BTC") && p.getCurrency2().equals("USD")).count());
        // pair "dusk:btc"
        Assert.assertEquals(1,
                pairs.stream().filter(p -> p.getCurrency1().equals("DUSK") && p.getCurrency2().equals("BTC")).count());
    }
}
