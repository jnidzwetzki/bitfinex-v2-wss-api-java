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

public class BitfinexTick implements Comparable<BitfinexTick>{
	
	/**
	 * The timestamp
	 */
	private final long timestamp;
	
	/**
	 * The open price
	 */
	private final BigDecimal open;
	
	/**
	 * The close price
	 */
	private final BigDecimal close;
	
	/**
	 * The high price
	 */
	private final BigDecimal high;
	
	/**
	 * The low price
	 */
	private final BigDecimal low;
	
	/**
	 * The volume
	 */
	private final BigDecimal volume;
	
	/**
	 * The invalid volume marker
	 */
	public final static BigDecimal INVALID_VOLUME = BigDecimal.valueOf(-1);

	public BitfinexTick(final long timestamp, BigDecimal open, BigDecimal close, BigDecimal high,
			BigDecimal low, BigDecimal volume) {
		
		assert (high.doubleValue() >= open.doubleValue()) : "High needs to be >= open";
		assert (high.doubleValue() >= close.doubleValue()) : "High needs to be => close";
		assert (low.doubleValue() <= open.doubleValue()) : "Low needs to be <= open";
		assert (low.doubleValue() <= close.doubleValue()) : "Low needs to be <= close";

		this.timestamp = timestamp;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = volume;
	}
	
	public BitfinexTick(final long timestamp, BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
		this(timestamp, open, close,  high, low, INVALID_VOLUME);
	}
	
	public BitfinexTick(final long timestamp, String open, String close, String high, String low) {
		this(timestamp, new BigDecimal(open), new BigDecimal(close), new BigDecimal(high), new BigDecimal(low), INVALID_VOLUME);
	}
	
	public BitfinexTick(final long timestamp, String open, String close, String high, String low, String volume) {
		this(timestamp, new BigDecimal(open), new BigDecimal(close), new BigDecimal(high), new BigDecimal(low), new BigDecimal(volume));
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Deprecated
	public double getOpen() {
		return open.doubleValue();
	}
	
	public BigDecimal getOpenAsBigDecimal() {
		return open;
	}

	@Deprecated
	public double getClose() {
		return close.doubleValue();
	}
	
	public BigDecimal getCloseAsBigDecimal() {
		return close;
	}

	@Deprecated
	public double getHigh() {
		return high.doubleValue();
	}
	
	public BigDecimal getHighAsBigDecimal() {
		return high;
	}

	@Deprecated
	public double getLow() {
		return low.doubleValue();
	}
	
	public BigDecimal getLowAsBigDecimal() {
		return low;
	}

	@Deprecated
	public double getVolume() {
		return volume.doubleValue();
	}
	
	public BigDecimal getVolumeAsBigDecimal() {
		return volume;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(close.doubleValue());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(high.doubleValue());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(low.doubleValue());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(open.doubleValue());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		temp = Double.doubleToLongBits(volume.doubleValue());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfinexTick other = (BitfinexTick) obj;
		if (Double.doubleToLongBits(close.doubleValue()) != Double.doubleToLongBits(other.close.doubleValue()))
			return false;
		if (Double.doubleToLongBits(high.doubleValue()) != Double.doubleToLongBits(other.high.doubleValue()))
			return false;
		if (Double.doubleToLongBits(low.doubleValue()) != Double.doubleToLongBits(other.low.doubleValue()))
			return false;
		if (Double.doubleToLongBits(open.doubleValue()) != Double.doubleToLongBits(other.open.doubleValue()))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (Double.doubleToLongBits(volume.doubleValue()) != Double.doubleToLongBits(other.volume.doubleValue()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bar [timestamp=" + timestamp + ", open=" + open + ", close=" + close + ", high=" + high + ", low=" + low
				+ ", volume=" + volume + "]";
	}

	@Override
	public int compareTo(final BitfinexTick otherTick) {
		return Long.compare(getTimestamp(), otherTick.getTimestamp());
	}
	
}
