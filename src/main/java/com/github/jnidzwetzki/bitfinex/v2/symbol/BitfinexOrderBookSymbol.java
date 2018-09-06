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
package com.github.jnidzwetzki.bitfinex.v2.symbol;

import java.util.Objects;

import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class BitfinexOrderBookSymbol implements BitfinexStreamSymbol {

	public enum Frequency {
		F0, // Realtime
		F1 // 2 sec
	}

	public enum Precision {
		R0,
		P0,
		P1,
		P2,
		P3
	}

	/**
	 * The currency pair
	 */
	private final BitfinexCurrencyPair currencyPair;

	/**
	 * The orderbook precision
	 */
	private final Precision orderBookPrecision;

	/**
	 * The orderbook frequency
	 */
	private final Frequency frequency;

	/**
	 * The amount of price points
	 */
	private final Integer pricePoints;


	BitfinexOrderBookSymbol(final BitfinexCurrencyPair currencyPair, final Precision orderBookPrecision,
							final Frequency frequency, final Integer pricePoints) {
		this.currencyPair = currencyPair;
		this.orderBookPrecision = orderBookPrecision;
		if (orderBookPrecision != Precision.R0) {
			this.frequency = frequency;
			if (pricePoints < 25 || pricePoints > 100) {
				throw new IllegalArgumentException("Price points must be between 25 and 100");
			}
			this.pricePoints = pricePoints;
		} else {
			this.frequency = null;
			this.pricePoints = null;

		}
	}

	public BitfinexCurrencyPair getCurrencyPair() {
		return currencyPair;
	}

	public Precision getPrecision() {
		return orderBookPrecision;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public Integer getPricePoints() {
		return pricePoints;
	}

	public boolean isRawOrderBook() {
		return orderBookPrecision == Precision.R0;
	}

	@Override
	public String toString() {
		return "BitfinexOrderBookSymbol [" +
				"currencyPair=" + currencyPair +
				", orderBookPrecision=" + orderBookPrecision +
				", frequency=" + frequency +
				", pricePoints=" + pricePoints +
				']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BitfinexOrderBookSymbol that = (BitfinexOrderBookSymbol) o;
		return Objects.equals(currencyPair, that.currencyPair) &&
				orderBookPrecision == that.orderBookPrecision &&
				frequency == that.frequency &&
				Objects.equals(pricePoints, that.pricePoints);
	}

	@Override
	public int hashCode() {
		return Objects.hash(currencyPair, orderBookPrecision, frequency, pricePoints);
	}

	/**
	 * Build from JSON Array
	 * @param jsonObject
	 * @return
	 */
	public static BitfinexOrderBookSymbol fromJSON(final JSONObject jsonObject) {
		BitfinexCurrencyPair symbol = BitfinexCurrencyPair.fromSymbolString(jsonObject.getString("symbol"));
		Precision prec = Precision.valueOf(jsonObject.getString("prec"));
		Frequency freq = null;
		Integer len = null;
		if (prec != Precision.R0) {
			freq = Frequency.valueOf(jsonObject.getString("freq"));
			len = jsonObject.getInt("len");
		}
		return new BitfinexOrderBookSymbol(symbol, prec, freq, len);
	}

}
