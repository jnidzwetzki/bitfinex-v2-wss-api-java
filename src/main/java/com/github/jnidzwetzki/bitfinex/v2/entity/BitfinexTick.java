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

public class BitfinexTick implements Comparable<BitfinexTick>{
	
	/**
	 * The timestamp
	 */
	private final long timestamp;
	
	/**
	 * The open price
	 */
	private final double open;
	
	/**
	 * The close price
	 */
	private final double close;
	
	/**
	 * The high price
	 */
	private final double high;
	
	/**
	 * The low price
	 */
	private final double low;
	
	/**
	 * The volume
	 */
	private final double volume;
	
	/**
	 * The invalid volume marker
	 */
	public final static double INVALID_VOLUME = -1;

	public BitfinexTick(final long timestamp, final double open, final double close, final double high,
			final double low, final double volume) {
		
		assert (high >= open);
		assert (high >= close);
		assert (low <= open);
		assert (low <= close);

		this.timestamp = timestamp;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = volume;
	}
	
	public BitfinexTick(final long timestamp, final double open, final double close, final double high, final double low) {
		this(timestamp, open, close,  high, low, INVALID_VOLUME);
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getVolume() {
		return volume;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(close);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(high);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(low);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(open);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		temp = Double.doubleToLongBits(volume);
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
		if (Double.doubleToLongBits(close) != Double.doubleToLongBits(other.close))
			return false;
		if (Double.doubleToLongBits(high) != Double.doubleToLongBits(other.high))
			return false;
		if (Double.doubleToLongBits(low) != Double.doubleToLongBits(other.low))
			return false;
		if (Double.doubleToLongBits(open) != Double.doubleToLongBits(other.open))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (Double.doubleToLongBits(volume) != Double.doubleToLongBits(other.volume))
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
