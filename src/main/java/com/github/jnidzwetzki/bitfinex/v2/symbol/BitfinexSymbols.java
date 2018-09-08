package com.github.jnidzwetzki.bitfinex.v2.symbol;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public final class BitfinexSymbols {

    private BitfinexSymbols() {

    }

    public static BitfinexCandlestickSymbol candlesticks(BitfinexCurrencyPair currencyPair, BitfinexCandleTimeFrame timeframe) {
        return new BitfinexCandlestickSymbol(currencyPair, timeframe);
    }

    public static BitfinexExecutedTradeSymbol executedTrades(BitfinexCurrencyPair currencyPair) {
        return new BitfinexExecutedTradeSymbol(currencyPair);
    }

    public static BitfinexOrderBookSymbol rawOrderBook(BitfinexCurrencyPair currencyPair) {
        return new BitfinexOrderBookSymbol(currencyPair, BitfinexOrderBookSymbol.Precision.R0, null, null);
    }

    public static BitfinexOrderBookSymbol orderBook(BitfinexCurrencyPair currencyPair, BitfinexOrderBookSymbol.Precision precision,
                                                    BitfinexOrderBookSymbol.Frequency frequency, int pricePoints) {
        if (precision == BitfinexOrderBookSymbol.Precision.R0) {
            throw new IllegalArgumentException("Use BitfinexSymbols#rawOrderBook() method instead");
        }
        return new BitfinexOrderBookSymbol(currencyPair, precision, frequency, pricePoints);
    }

    public static BitfinexTickerSymbol ticker(BitfinexCurrencyPair currencyPair) {
        return new BitfinexTickerSymbol(currencyPair);
    }
}
