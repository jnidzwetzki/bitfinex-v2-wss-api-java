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

import java.math.BigDecimal;

public class BitfinexTick {

	/**
	 * The bid
	 */
	private final BigDecimal bid;

	/**
	 * The bid size
	 */
	private final BigDecimal bidSize;

	/**
	 * The ask
	 */
	private final BigDecimal ask;

	/**
	 * The bid size
	 */
	private final BigDecimal askSize;

	/**
	 * The daily change
	 */
	private final BigDecimal dailyChange;

	/**
	 * The daily change in percent
	 */
	private final BigDecimal dailyChangePerc;

	/**
	 * The last price
	 */
	private final BigDecimal lastPrice;

	/**
	 * The volume
	 */
	private final BigDecimal volume;

	/**
	 * The daily high
	 */
	private final BigDecimal high;

	/**
	 * The daily low
	 */
	private final BigDecimal low;

	public BitfinexTick(final BigDecimal bid, final BigDecimal bidSize, final BigDecimal ask,
			final BigDecimal askSize, final BigDecimal dailyChange, final BigDecimal dailyChangePerc,
			final BigDecimal lastPrice, final BigDecimal volume, final BigDecimal high, final BigDecimal low) {

		this.bid = bid;
		this.bidSize = bidSize;
		this.ask = ask;
		this.askSize = askSize;
		this.dailyChange = dailyChange;
		this.dailyChangePerc = dailyChangePerc;
		this.lastPrice = lastPrice;
		this.volume = volume;
		this.high = high;
		this.low = low;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public BigDecimal getBidSize() {
		return bidSize;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public BigDecimal getAskSize() {
		return askSize;
	}

	public BigDecimal getDailyChange() {
		return dailyChange;
	}

	public BigDecimal getDailyChangePerc() {
		return dailyChangePerc;
	}

	public BigDecimal getLastPrice() {
		return lastPrice;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public BigDecimal getLow() {
		return low;
	}

	@Override
	public String toString() {
		return "BitfinexTick [bid=" + bid + ", bidSize=" + bidSize + ", ask=" + ask + ", askSize=" + askSize
				+ ", dailyChange=" + dailyChange + ", dailyChangePerc=" + dailyChangePerc + ", lastPrice=" + lastPrice
				+ ", volume=" + volume + ", high=" + high + ", low=" + low + "]";
	}

}
