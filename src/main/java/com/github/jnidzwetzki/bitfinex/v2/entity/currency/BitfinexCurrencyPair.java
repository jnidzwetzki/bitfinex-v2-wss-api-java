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
package com.github.jnidzwetzki.bitfinex.v2.entity.currency;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bboxdb.commons.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class BitfinexCurrencyPair implements BitfinexInstrument {

	/**
	 * The known currencies
	 */
	private final static Map<String, BitfinexCurrencyPair> instances = new ConcurrentHashMap<>();

	/**
	 * The Bitfinex symbol URL
	 */
	public static final String SYMBOL_URL = "https://api.bitfinex.com/v1/symbols_details";

	/**
	 * Load and register all known currencies
	 *
	 * @throws BitfinexClientException
	 */
	public static void registerDefaults() throws BitfinexClientException {

		try {
			final URL url = new URL(SYMBOL_URL);
			final String symbolJson = Resources.toString(url, Charsets.UTF_8);
			final JSONArray jsonArray = new JSONArray(symbolJson);

			for(int i = 0; i < jsonArray.length(); i++) {
				final JSONObject currency = jsonArray.getJSONObject(i);
				final String pair = currency.getString("pair");

                final Pair<String, String> pairs = parsePair(pair);
                final double minOrderSize = currency.getDouble("minimum_order_size");
                final BitfinexCurrencyType type = parseCurrencyType(pairs);
                register(pairs.getElement1(), pairs.getElement2(), type, minOrderSize);
            }
			
        } catch (IOException e) {
            throw new BitfinexClientException(e);
        }
    }

	/**
	 * Parse the currency type
	 * @param pairs
	 * @return the type of the currency
	 */
    private static BitfinexCurrencyType parseCurrencyType(final Pair<String, String> pairs) {
    	
    	final String PERPETUAL_END = "F0".toLowerCase();
    	
		if(pairs.getElement1().toLowerCase().endsWith(PERPETUAL_END) 
				&& pairs.getElement2().toLowerCase().endsWith(PERPETUAL_END)) {
			return BitfinexCurrencyType.PERPETUAL;
		}
    	
		return BitfinexCurrencyType.CURRENCY;
	}

	/**
     * Parse the currency pair. Some new pairs contain ':' and are longer than 6 chars, e.g. "dusk:usd".
     * Pairs with the format *f0:*f0 (e.g. btcf0:ustf0) are 'perpetual contracts'
     * @param pair bitfinex's currency pair.
     * @return A {@link Pair} with currency1 as first and currency2 as second element.
     */
    private static Pair<String, String> parsePair(final String pair) throws BitfinexClientException{
        final int idx = pair.indexOf(":");

        final String currency1;
        final String currency2;

        if (idx > -1) {
            currency1 = pair.substring(0, idx).toUpperCase();
            currency2 = pair.substring(idx + 1).toUpperCase();
        } else {
            if (pair.length() != 6) {
                throw new BitfinexClientException("The currency pair is not 6 chars long: " + pair);
            }
            currency1 = pair.substring(0, 3).toUpperCase();
            currency2 = pair.substring(3, 6).toUpperCase();
        }
        return new Pair<>(currency1, currency2);
    }

	public static void unregisterAll() {
		instances.clear();
	}

	/**
	 * Registers currency pair for use within library
	 *
	 * @param currency         currency (from)
	 * @param profitCurrency   currency (to)
	 * @param BitfinexCurrencyType the currency type
	 * @param minimalOrderSize minimal order size
	 * @return registered instance of {@link BitfinexCurrencyPair}
	 */
	public static BitfinexCurrencyPair register(final String currency,
			final String profitCurrency, final BitfinexCurrencyType type, final double minimalOrderSize) {

		final String key = buildCacheKey(currency, profitCurrency);

		final BitfinexCurrencyPair newCurrency = new BitfinexCurrencyPair(currency, profitCurrency,
				type, minimalOrderSize);

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
	 * The currency type
	 */
	private final BitfinexCurrencyType currencyType;

	/**
	 * The minimum order size
	 */
	private double minimumOrderSize;

	private BitfinexCurrencyPair(final String pair1, final String pair2, 
			final BitfinexCurrencyType currencyType, final double minimumOrderSize) {
		
		this.currency1 = pair1;
		this.currency2 = pair2;
		this.currencyType = currencyType;
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
	 * Get the currency type
	 * @return
	 */
	public BitfinexCurrencyType getCurrencyType() {
		return currencyType;
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
	 * Convert to bitfinex string (t means trading pair)
	 * @return
	 */
	@Override
	public String toBitfinexString() {
		if(currencyType == BitfinexCurrencyType.PERPETUAL) {
			return "t" + currency1 + ":" + currency2;
		}
		
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

	@Override
	public String toString() {
		return "BitfinexCurrencyPair [currency1=" + currency1 + ", currency2=" + currency2 + ", currencyType="
				+ currencyType + ", minimumOrderSize=" + minimumOrderSize + "]";
	}

}
