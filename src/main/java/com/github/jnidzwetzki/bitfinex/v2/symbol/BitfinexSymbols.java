package com.github.jnidzwetzki.bitfinex.v2.symbol;

import java.util.Objects;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

/**
 * Bitfinex symbol factory class
 */
public final class BitfinexSymbols {

    private BitfinexSymbols() {

    }

    /**
     * Returns symbol for account.
     * used only within lib - no practical use for end-user
     *
     * @param apiKey      for this account
     * @param permissions of specified key
     * @return symbol
     */
    public static BitfinexAccountSymbol account(String apiKey, BitfinexApiKeyPermissions permissions) {
        return new BitfinexAccountSymbol(apiKey, permissions);
    }

    /**
     * Returns symbol for candlestick channel
     *
     * @param currencyPair of candles
     * @param timeframe    configuration of candles
     * @return symbol
     */
    public static BitfinexCandlestickSymbol candlesticks(BitfinexCurrencyPair currencyPair, BitfinexCandleTimeFrame timeframe) {
        return new BitfinexCandlestickSymbol(currencyPair, Objects.requireNonNull(timeframe));
    }

    /**
     * Returns symbol for candlestick channel
     *
     * @param currency       of candles
     * @param profitCurrency of candles
     * @param timeframe      configuration of candles
     * @return symbol
     */
    public static BitfinexCandlestickSymbol candlesticks(String currency, String profitCurrency, BitfinexCandleTimeFrame timeframe) {
        currency = Objects.requireNonNull(currency).toUpperCase();
        profitCurrency = Objects.requireNonNull(profitCurrency).toUpperCase();
        return candlesticks(BitfinexCurrencyPair.of(currency, profitCurrency), timeframe);
    }

    /**
     * Returns symbol for executed trades channel
     *
     * @param currencyPair of trades channel
     * @return symbol
     */
    public static BitfinexExecutedTradeSymbol executedTrades(BitfinexCurrencyPair currencyPair) {
        return new BitfinexExecutedTradeSymbol(currencyPair);
    }

    /**
     * Returns symbol for candlestick channel
     *
     * @param currency       of trades channel
     * @param profitCurrency of trades channel
     * @return symbol
     */
    public static BitfinexExecutedTradeSymbol executedTrades(String currency, String profitCurrency) {
        currency = Objects.requireNonNull(currency).toUpperCase();
        profitCurrency = Objects.requireNonNull(profitCurrency).toUpperCase();
        return executedTrades(BitfinexCurrencyPair.of(currency, profitCurrency));
    }

    /**
     * returns symbol for raw order book channel
     *
     * @param currencyPair of raw order book channel
     * @return symbol
     */
    public static BitfinexOrderBookSymbol rawOrderBook(BitfinexCurrencyPair currencyPair) {
        return new BitfinexOrderBookSymbol(currencyPair, BitfinexOrderBookSymbol.Precision.R0, null, null);
    }

    /**
     * Returns symbol for raw order book channel
     *
     * @param currency       of raw order book channel
     * @param profitCurrency of raw order book channel
     * @return symbol
     */
    public static BitfinexOrderBookSymbol rawOrderBook(String currency, String profitCurrency) {
        currency = Objects.requireNonNull(currency).toUpperCase();
        profitCurrency = Objects.requireNonNull(profitCurrency).toUpperCase();
        return rawOrderBook(BitfinexCurrencyPair.of(currency, profitCurrency));
    }

    /**
     * returns symbol for order book channel
     *
     * @param currencyPair of order book channel
     * @param precision    of order book
     * @param frequency    of order book
     * @param pricePoints  in initial snapshot
     * @return symbol
     */
    public static BitfinexOrderBookSymbol orderBook(BitfinexCurrencyPair currencyPair, BitfinexOrderBookSymbol.Precision precision,
                                                    BitfinexOrderBookSymbol.Frequency frequency, int pricePoints) {
        if (precision == BitfinexOrderBookSymbol.Precision.R0) {
            throw new IllegalArgumentException("Use BitfinexSymbols#rawOrderBook() factory method instead");
        }
        return new BitfinexOrderBookSymbol(currencyPair, precision, frequency, pricePoints);
    }

    /**
     * Returns symbol for candlestick channel
     *
     * @param currency       of order book
     * @param profitCurrency of order book
     * @param precision      of order book
     * @param frequency      of order book
     * @param pricePoints    in initial snapshot
     * @return symbol
     */
    public static BitfinexOrderBookSymbol orderBook(String currency, String profitCurrency, BitfinexOrderBookSymbol.Precision precision,
                                                    BitfinexOrderBookSymbol.Frequency frequency, int pricePoints) {
        currency = Objects.requireNonNull(currency).toUpperCase();
        profitCurrency = Objects.requireNonNull(profitCurrency).toUpperCase();
        return orderBook(BitfinexCurrencyPair.of(currency, profitCurrency), precision, frequency, pricePoints);
    }

    /**
     * returns symbol for ticker channel
     *
     * @param currencyPair of ticker channel
     * @return symbol
     */
    public static BitfinexTickerSymbol ticker(BitfinexCurrencyPair currencyPair) {
        return new BitfinexTickerSymbol(currencyPair);
    }

    /**
     * returns symbol for ticker channel
     *
     * @param currency       of ticker
     * @param profitCurrency of ticker
     * @return symbol
     */
    public static BitfinexTickerSymbol ticker(String currency, String profitCurrency) {
        currency = Objects.requireNonNull(currency).toUpperCase();
        profitCurrency = Objects.requireNonNull(profitCurrency).toUpperCase();
        return ticker(BitfinexCurrencyPair.of(currency, profitCurrency));
    }
}
