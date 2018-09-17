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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class BitfinexCurrencyPair {

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

				if(pair.length() != 6) {
					throw new BitfinexClientException("The currency pair is not 6 chars long: " + pair);
				}

				final double minOrderSize = currency.getDouble("minimum_order_size");
				final String currency1 = pair.substring(0, 3).toUpperCase();
				final String currency2 = pair.substring(3, 6).toUpperCase();
				register(currency1, currency2, minOrderSize);
			}

		} catch (IOException e) {
			throw new BitfinexClientException(e);
		} 
	}

	public static void unregisterAll() {
		instances.clear();
	}

	/**
	 * Registers currency pair for use within library
	 *
	 * @param currency         currency (from)
	 * @param profitCurrency   currency (to)
	 * @param minimalOrderSize minimal order size
	 * @return registered instance of {@link BitfinexCurrencyPair}
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

	@Override
	public String toString() {
		return currency1 + ":" + currency2;
	}
}
