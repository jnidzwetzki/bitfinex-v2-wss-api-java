package com.github.jnidzwetzki.bitfinex.v2.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexFundingCurrency;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexExecutedTradeSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexFundingSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexTickerSymbol;

public class BitfinexSymbolsTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();	
		}
	}

	/**
	 * Are the default currencies loaded?
	 */
	@Test
	public void testDefaultCurrencyLoaded() {
		final BitfinexCurrencyPair currency = BitfinexCurrencyPair.of("BTC", "USD");
		Assert.assertNotNull(currency);
		Assert.assertTrue(BitfinexCurrencyPair.values().size() > 20);
	}

	/**
	 * Test the creation of symbols
	 * @throws BitfinexCommandException
	 */
	@Test
	public void testSymbolCreation() throws BitfinexCommandException {
		  BitfinexCurrencyPair.values().stream()
		  	.limit(20)
		  	.forEach(bfxPair -> {
              final BitfinexCandlestickSymbol candlesticks1 = BitfinexSymbols.candlesticks(bfxPair, BitfinexCandleTimeFrame.MINUTES_1);
              final BitfinexCandlestickSymbol candlesticks2 = BitfinexSymbols.candlesticks(bfxPair.getCurrency1(), bfxPair.getCurrency2(), BitfinexCandleTimeFrame.MINUTES_1);
              Assert.assertEquals(candlesticks1, candlesticks2);
              Assert.assertNotEquals(null, candlesticks1);
              
              final BitfinexExecutedTradeSymbol trades1 = BitfinexSymbols.executedTrades(bfxPair);
              final BitfinexExecutedTradeSymbol trades2 = BitfinexSymbols.executedTrades(bfxPair.getCurrency1(), bfxPair.getCurrency2());
              Assert.assertEquals(trades1, trades2);
              Assert.assertNotEquals(null, trades1);

              final BitfinexOrderBookSymbol rawOrderBook1 = BitfinexSymbols.rawOrderBook(bfxPair);
              final BitfinexOrderBookSymbol rawOrderBook2 = BitfinexSymbols.rawOrderBook(bfxPair.getCurrency1(), bfxPair.getCurrency2());
              Assert.assertEquals(rawOrderBook1, rawOrderBook2);
              Assert.assertNotEquals(null, rawOrderBook1);

              final BitfinexOrderBookSymbol orderBook1 = BitfinexSymbols.orderBook(bfxPair, BitfinexOrderBookSymbol.Precision.P0, BitfinexOrderBookSymbol.Frequency.F0, 100);
              final BitfinexOrderBookSymbol orderBook2 = BitfinexSymbols.orderBook(bfxPair.getCurrency1(), bfxPair.getCurrency2(), BitfinexOrderBookSymbol.Precision.P0, BitfinexOrderBookSymbol.Frequency.F0, 100);
              Assert.assertEquals(orderBook1, orderBook2);
              Assert.assertNotEquals(null, orderBook1);

              final BitfinexTickerSymbol ticker1 = BitfinexSymbols.ticker(bfxPair);
              final BitfinexTickerSymbol ticker2 = BitfinexSymbols.ticker(bfxPair.getCurrency1(), bfxPair.getCurrency2());
              Assert.assertEquals(ticker1, ticker2);
              Assert.assertNotEquals(null, ticker1);
          });	
	}
	
	/**
	 * Test the creation of symbols
	 * @throws BitfinexCommandException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testSymbolCreationInvalid() throws BitfinexCommandException {
        BitfinexSymbols.orderBook(BitfinexCurrencyPair.of("BTC", "USD"), BitfinexOrderBookSymbol.Precision.R0, BitfinexOrderBookSymbol.Frequency.F0, 100);
	}
	
	/**
	 * Test the funding symbol
	 */
	@Test
	public void testFundingSymbol() {
		final BitfinexFundingSymbol symbol1 = BitfinexSymbols.funding("USD");
		final BitfinexFundingSymbol symbol2 = BitfinexSymbols.funding(new BitfinexFundingCurrency("USD"));
		
		Assert.assertEquals(symbol1, symbol2);
	}
	
	/**
	 * Test the funding currency
	 */
	@Test
	public void testFundingCurrency1() {
		final BitfinexFundingCurrency currency1 = new BitfinexFundingCurrency("USD");
		final BitfinexFundingCurrency currency2 = BitfinexFundingCurrency.fromSymbolString("fUSD");
		
		Assert.assertTrue(currency1.toString().length() > 10);
		Assert.assertEquals(currency1, currency2);
		Assert.assertEquals(currency1.hashCode(), currency2.hashCode());
		
		Assert.assertEquals("fUSD", currency1.toBitfinexString());
	}
	
	/**
	 * Test the funding currency
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testFundingCurrency2() {
		BitfinexFundingCurrency.fromSymbolString("USD");
	}
}
