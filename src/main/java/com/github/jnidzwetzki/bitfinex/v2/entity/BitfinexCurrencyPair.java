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
package com.github.jnidzwetzki.bitfinex.v2.entity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitfinexCurrencyPair {

	private final static Map<String, BitfinexCurrencyPair> instances = new ConcurrentHashMap<>();

	static {
		register("BTC", "USD", 0.002);
		register("BTC", "JPY", 0.002);
		register("BTC", "EUR", 0.002);
		register("BTC", "GBP", 0.002);
		register("LTC", "USD", 0.08);
		register("LTC", "BTC", 0.08);
		register("ETH", "USD", 0.02);
		register("ETH", "BTC", 0.02);
		register("ETH", "JPY", 0.002);
		register("ETH", "EUR", 0.002);
		register("ETH", "GBP", 0.002);
		register("ETC", "BTC", 0.6);
		register("ETC", "USD", 0.6);
		register("RRT", "USD", 80.0);
		register("RRT", "BTC", 80.0);
		register("ZEC", "USD", 0.04);
		register("ZEC", "BTC", 0.04);
		register("XMR", "USD", 0.06);
		register("XMR", "BTC", 0.06);
		register("DSH", "USD", 0.02);
		register("DSH", "BTC", 0.02);
		register("XRP", "USD", 10.0);
		register("XRP", "BTC", 10.0);
		register("IOT", "USD", 6.0);
		register("IOT", "BTC", 6.0);
		register("IOT", "ETH", 6.0);
		register("IOT", "JPY", 6.0);
		register("IOT", "EUR", 6.0);
		register("IOT", "GBP", 6.0);
		register("EOS", "USD", 2.0);
		register("EOS", "BTC", 2.0);
		register("EOS", "ETH", 2.0);
		register("EOS", "JPY", 2.0);
		register("EOS", "EUR", 2.0);
		register("EOS", "GBP", 2.0);
		register("SAN", "USD", 4.0);
		register("SAN", "BTC", 4.0);
		register("SAN", "ETH", 4.0);
		register("OMG", "USD", 1.0);
		register("OMG", "BTC", 1.0);
		register("OMG", "ETH", 1.0);
		register("BCH", "USD", 0.006);
		register("BCH", "BTC", 0.006);
		register("BCH", "ETH", 0.006);
		register("NEO", "USD", 0.2);
		register("NEO", "BTC", 0.2);
		register("NEO", "ETH", 0.2);
		register("NEO", "JPY", 0.2);
		register("NEO", "EUR", 0.2);
		register("NEO", "GBP", 0.2);
		register("ETP", "USD", 4.0);
		register("ETP", "BTC", 4.0);
		register("ETP", "ETH", 4.0);
		register("QTM", "USD", 0.4);
		register("QTM", "BTC", 0.4);
		register("QTM", "ETH", 0.4);
		register("AVT", "USD", 4.0);
		register("AVT", "BTC", 4.0);
		register("AVT", "ETH", 4.0);
		register("EDO", "USD", 4.0);
		register("EDO", "BTC", 4.0);
		register("EDO", "ETH", 4.0);
		register("BTG", "USD", 0.06);
		register("BTG", "BTC", 0.06);
		register("DAT", "USD", 74.0);
		register("DAT", "BTC", 74.0);
		register("DAT", "ETH", 74.0);
		register("QSH", "USD", 10.0);
		register("QSH", "BTC", 10.0);
		register("QSH", "ETH", 10.0);
		register("YYW", "USD", 48.0);
		register("YYW", "BTC", 48.0);
		register("YYW", "ETH", 48.0);
		register("GNT", "USD", 16.0);
		register("GNT", "BTC", 16.0);
		register("GNT", "ETH", 16.0);
		register("SNT", "USD", 38.0);
		register("SNT", "BTC", 38.0);
		register("SNT", "ETH", 38.0);
		register("BAT", "USD", 18.0);
		register("BAT", "BTC", 18.0);
		register("BAT", "ETH", 18.0);
		register("MNA", "USD", 80.0);
		register("MNA", "BTC", 80.0);
		register("MNA", "ETH", 80.0);
		register("FUN", "USD", 108.0);
		register("FUN", "BTC", 108.0);
		register("FUN", "ETH", 108.0);
		register("ZRX", "USD", 6.0);
		register("ZRX", "BTC", 6.0);
		register("ZRX", "ETH", 6.0);
		register("TNB", "USD", 104.0);
		register("TNB", "BTC", 104.0);
		register("TNB", "ETH", 104.0);
		register("SPK", "USD", 26.0);
		register("SPK", "BTC", 26.0);
		register("SPK", "ETH", 26.0);
		register("TRX", "BTC", 26.0);
		register("TRX", "ETH", 26.0);
		register("TRX", "USD", 26.0);
		register("RCN", "BTC", 26.0);
		register("RCN", "ETH", 26.0);
		register("RCN", "USD", 26.0);
		register("RLC", "BTC", 26.0);
		register("RLC", "ETH", 26.0);
		register("RLC", "USD", 26.0);
		register("AID", "BTC", 26.0);
		register("AID", "USD", 26.0);
		register("AID", "ETH", 26.0);
		register("SNG", "BTC", 26.0);
		register("SNG", "ETH", 26.0);
		register("SNG", "USD", 26.0);
		register("REP", "BTC", 26.0);
		register("REP", "ETH", 26.0);
		register("REP", "USD", 26.0);
		register("ELF", "USD", 26.0);
		register("ELF", "BTC", 26.0);
		register("ELF", "ETH", 26.0);
		register("BFT", "USD", 26.0);
		register("BFT", "BTC", 26.0);
		register("BFT", "ETH", 26.0);
		register("IOS", "USD", 26.0);
		register("IOS", "BTC", 26.0);
		register("IOS", "ETH", 26.0);
		register("AIO", "USD", 26.0);
		register("AIO", "BTC", 26.0);
		register("AIO", "ETH", 26.0);
		register("REQ", "USD", 26.0);
		register("REQ", "BTC", 26.0);
		register("REQ", "ETH", 26.0);
		register("RDN", "USD", 26.0);
		register("RDN", "BTC", 26.0);
		register("RDN", "ETH", 26.0);
		register("LRC", "USD", 26.0);
		register("LRC", "BTC", 26.0);
		register("LRC", "ETH", 26.0);
		register("WAX", "USD", 26.0);
		register("WAX", "BTC", 26.0);
		register("WAX", "ETH", 26.0);
		register("DAI", "USD", 26.0);
		register("DAI", "BTC", 26.0);
		register("DAI", "ETH", 26.0);
		register("CFI", "USD", 26.0);
		register("CFI", "BTC", 26.0);
		register("CFI", "ETH", 26.0);
		register("AGI", "USD", 26.0);
		register("AGI", "BTC", 26.0);
		register("AGI", "ETH", 26.0);
		register("MTN", "USD", 26.0);
		register("MTN", "BTC", 26.0);
		register("MTN", "ETH", 26.0);
		register("ODE", "USD", 26.0);
		register("ODE", "BTC", 26.0);
		register("ODE", "ETH", 26.0);
		register("ANT", "USD", 26.0);
		register("ANT", "BTC", 26.0);
		register("ANT", "ETH", 26.0);
		register("MIT", "USD", 26.0);
		register("MIT", "BTC", 26.0);
		register("MIT", "ETH", 26.0);
		register("DTH", "USD", 26.0);
		register("DTH", "BTC", 26.0);
		register("DTH", "ETH", 26.0);
		register("STJ", "USD", 26.0);
		register("STJ", "BTC", 26.0);
		register("STJ", "ETH", 26.0);
		register("XLM", "USD", 26.0);
		register("XLM", "BTC", 26.0);
		register("XLM", "ETH", 26.0);
		register("XLM", "EUR", 26.0);
		register("XLM", "GBP", 26.0);
		register("XLM", "JPY", 26.0);
		register("XVG", "USD", 26.0);
		register("XVG", "BTC", 26.0);
		register("XVG", "ETH", 26.0);
		register("XVG", "EUR", 26.0);
		register("XVG", "GBP", 26.0);
		register("XVG", "JPY", 26.0);
		register("BCI", "USD", 26.0);
		register("BCI", "BTC", 26.0);
		register("BCI", "ETH", 26.0);
	}

	/**
	 * Registers currency pair for use within library
	 *
	 * @param currency         currency (from)
	 * @param profitCurrency   currency (to)
	 * @param minimalOrderSize minimal order size
	 * @return 
	 */
	public static BitfinexCurrencyPair register(final String currency, 
			final String profitCurrency, final double minimalOrderSize) {

		final String key = buildCacheKey(currency, profitCurrency);

		final BitfinexCurrencyPair newCurrency = new BitfinexCurrencyPair(currency, profitCurrency, 
				minimalOrderSize);

		final BitfinexCurrencyPair oldCurrency = instances.putIfAbsent(key, newCurrency);
		
		// The currency was already registered
		if(oldCurrency != null) {
			throw new IllegalArgumentException("The currency " + key + " is already known");
		}
		
		return newCurrency;
	}

	/**
	 * Retrieves bitfinex currency pair
	 *
	 * @param currency       currency (from)
	 * @param profitCurrency currency (to)
	 * @return BitfinexCurrencyPair
	 */
	public static BitfinexCurrencyPair of(final String currency, final String profitCurrency) {
		final String key = buildCacheKey(currency, profitCurrency);

		final BitfinexCurrencyPair bcp = instances.get(key);

		if (bcp == null) {
			throw new IllegalArgumentException("CurrencyPair is not registered: " + currency + " " + profitCurrency);
		}

		return bcp;
	}

	/**
	 * Build the cache key
	 * 
	 * @param currency1
	 * @param currency2
	 * @return
	 */
	private static String buildCacheKey(final String currency1, final String currency2) {
		return currency1 + "_" + currency2;
	}

	/**
	 * lists all available pairs
	 *
	 * @return list of BitfinexCurrencyPair
	 */
	public static Collection<BitfinexCurrencyPair> values() {
		return instances.values();
	}

	/**
	 * The name of the first currency 
	 */
	private final String currency1;

	/**
	 * The name of the second currency
	 */
	private final String currency2;

	/**
	 * The minimum order size
	 */
	private double minimumOrderSize;

	private BitfinexCurrencyPair(final String pair1, final String pair2, final double minimumOrderSize) {
		this.currency1 = pair1;
		this.currency2 = pair2;
		this.minimumOrderSize = minimumOrderSize;
	}

	/**
	 * Get the minimum order size
	 * @return
	 */
	public double getMinimumOrderSize() {
		return minimumOrderSize;
	}

	/**
	 * Set the minimum order size
	 * @param minimumOrderSize
	 */
	public void setMinimumOrderSize(final double minimumOrderSize) {
		this.minimumOrderSize = minimumOrderSize;
	}

	/**
	 * Construct from string
	 * @param symbolString
	 * @return
	 */
	public static BitfinexCurrencyPair fromSymbolString(final String symbolString) {
		for (final BitfinexCurrencyPair currency : BitfinexCurrencyPair.values()) {
			if (currency.toBitfinexString().equalsIgnoreCase(symbolString)) {
				return currency;
			}
		}
		throw new IllegalArgumentException("Unable to find currency pair for: " + symbolString);
	}

	/**
	 * Convert to bitfinex string
	 * @return
	 */
	public String toBitfinexString() {
		return "t" + currency1 + currency2;
	}

	/**
	 * Get the first currency
	 * @return
	 */
	public String getCurrency1() {
		return currency1;
	}

	/**
	 * Set the second currency
	 * @return
	 */
	public String getCurrency2() {
		return currency2;
	}
}
