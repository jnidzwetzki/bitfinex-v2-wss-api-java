package com.github.jnidzwetzki.bitfinex.v2.entity;

import java.util.concurrent.TimeUnit;

public enum Timeframe {

	MINUTES_1(TimeUnit.MINUTES.toMillis(1), "1m"),
	MINUTES_5(TimeUnit.MINUTES.toMillis(5), "5m"),
	MINUTES_15(TimeUnit.MINUTES.toMillis(15), "15m"),
	MINUTES_30(TimeUnit.MINUTES.toMillis(30), "30m"),
	HOUR_1(TimeUnit.HOURS.toMillis(1), "1h");
	
	private Timeframe(final long milliseconds, final String bitfinexString) {
		this.milliseconds = milliseconds;
		this.bitfinexString = bitfinexString;
	}
	
	private final long milliseconds;
	
	private final String bitfinexString;

	public long getMilliSeconds() {
		return milliseconds;
	}
	
	public String getBitfinexString() {
		return bitfinexString;
	}
	
	/**
	 * Construct from symbol string
	 * @param symbolString
	 * @return
	 */
	public static Timeframe fromSymbolString(final String symbolString) {
		for (final Timeframe timeframe : Timeframe.values()) {
			if (timeframe.getBitfinexString().equalsIgnoreCase(symbolString)) {
				return timeframe;
			}
		}
		throw new IllegalArgumentException("Unable to find timeframe for: " + symbolString);
	}
}
