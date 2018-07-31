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
import java.util.Optional;

public class BitfinexCandle implements Comparable<BitfinexCandle>{
	
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
	private final Optional<BigDecimal> volume;
	
	public BitfinexCandle(final long timestamp, final BigDecimal open, final BigDecimal close, 
			final BigDecimal high, final BigDecimal low, final Optional<BigDecimal> volume) {
		
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
	
	public BitfinexCandle(final long timestamp, final BigDecimal open, final BigDecimal close, 
			final BigDecimal high, final BigDecimal low) {
		
		this(timestamp, open, close,  high, low, Optional.empty());
	}
	
	public BitfinexCandle(final long timestamp, final BigDecimal open, final BigDecimal close, 
			final BigDecimal high, final BigDecimal low, final BigDecimal volume) {
		
		this(timestamp, open, close,  high, low, Optional.of(volume));
	}
	
	public BitfinexCandle(final long timestamp, final double open, final double close, 
			final double high, final double low, final double volume) {
		
		this(timestamp, new BigDecimal(open), new BigDecimal(close),  new BigDecimal(high), 
				new BigDecimal(low), Optional.of(new BigDecimal(volume)));
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public BigDecimal getHigh() {
		return high;
	}
	
	public BigDecimal getLow() {
		return low;
	}

	public Optional<BigDecimal> getVolume() {
		return volume;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((close == null) ? 0 : close.hashCode());
		result = prime * result + ((high == null) ? 0 : high.hashCode());
		result = prime * result + ((low == null) ? 0 : low.hashCode());
		result = prime * result + ((open == null) ? 0 : open.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
		BitfinexCandle other = (BitfinexCandle) obj;
		if (close == null) {
			if (other.close != null)
				return false;
		} else if (!close.equals(other.close))
			return false;
		if (high == null) {
			if (other.high != null)
				return false;
		} else if (!high.equals(other.high))
			return false;
		if (low == null) {
			if (other.low != null)
				return false;
		} else if (!low.equals(other.low))
			return false;
		if (open == null) {
			if (other.open != null)
				return false;
		} else if (!open.equals(other.open))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bar [timestamp=" + timestamp + ", open=" + open + ", close=" + close + ", high=" + high + ", low=" + low
				+ ", volume=" + volume + "]";
	}

	@Override
	public int compareTo(final BitfinexCandle otherTick) {
		return Long.compare(getTimestamp(), otherTick.getTimestamp());
	}
	
}
