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
package com.github.jnidzwetzki.bitfinex.v2.entity.symbol;

public enum BitfinexCurrencyPair implements BitfinexStreamSymbol {

	// Bitcoin
	BTC_USD("BTC", "USD", 0.002),
	BTC_EUR("BTC", "EUR", 0.002),
	
	// Litecoin
	LTC_USD("LTC", "USD", 0.2),
	LTC_BTC("LTC", "BTC", 0.2),
	
	// Ethereum
	ETH_USD("ETH", "USD", 0.04),
	ETH_BTC("ETH", "BTC", 0.04),
	
	// Ethereum classic
	ETC_BTC("ETC", "BTC", 0.2),
	ETC_BCH("ETC", "BCH", 0.2),
	
	// ZCash
	ZEC_USD("ZEC", "USD", 0.2),
	ZEC_BTC("ZEC", "BTC", 0.2),
	
	// Monero
	XMR_USD("XMR", "USD", 0.2),
	XMR_BTC("XMR", "BTC", 0.2),
	
	// Dash
	DCH_USD("DCH", "USD", 0.2),
	DCH_BTC("DCH", "BTC", 0.2),
	
	// XRP 
	XRP_USD("XRP", "USD", 22.0),
	XRP_BTC("XRP", "BTC", 0.2),
	
	// IOTA
	IOTA_USD("IOT", "USD", 6.0),
	IOTA_BTC("IOT", "BTC", 0.2),
	IOTA_ETH("IOT", "ETH", 0.2),
	IOTA_EUR("IOT", "EUR", 5.0),
	
	// EOS
	EOS_USD("EOS", "USD", 0.2),
	EOS_BTC("EOS", "BTC", 0.2),
	EOS_BHC("EOS", "BCH", 0.2),
	
	// Bitcoin Cash
	BCH_USD("BCH", "USD", 0.02),
	BCH_BTC("BCH", "BTC", 0.02),
	BCH_ETH("BCH", "ETH", 0.02),

	// NEO
	NEO_USD("NEO", "USD", 0.2),
	NEO_BTC("NEO", "BTC", 0.2),
	NEO_ETH("NEO", "ETH", 0.2),
	
	// Eidoo
	EDO_USD("EDO", "USD", 0.2),
	EDO_BTC("EDO", "BTC", 0.2),
	EDO_ETH("EDO", "ETH", 0.2),
	
	// Santiment Network Token
	SAN_USD("SAN", "USD", 0.2),
	SAN_BTC("SAN", "BTC", 0.2),
	SAN_ETH("SAN", "ETH", 0.2),
	
	// Recovery Right Tokens
	RRT_USD("RRT", "USD", 72),
	RRT_BTC("RRT", "BTC", 72),
	
	// FunFair
	FUN_USD("FUN", "USD", 64),
	FUN_BTC("FUN", "BTC", 64),
	FUN_ETH("FUN", "ETH", 64),
	
	// Streamr
	DATA_USD("DATA", "USD", 74),
	DATA_BTC("DATA", "BTC", 74),
	DATA_ETH("DATA", "ETH", 74),
	
	// Time New Bank
	TNB_USD("TNB", "USD", 46),
	TNB_BTC("TNB", "BTC", 46),
	TNB_ETH("TNB", "ETH", 46),
	
	// Decentraland
	MNA_USD("MNA", "USD", 80),
	MNA_BTC("MNA", "BTC", 80),
	MNA_ETH("MNA", "ETH", 80),
	
	// YOYOW
	YYW_USD("YYW", "USD", 48),
	YYW_BTC("YYW", "BTC", 48),
	YYW_ETH("YYW", "ETH", 48),
	
	// Status
	SNT_USD("SNT", "USD", 38),
	SNT_BTC("SNT", "BTC", 38),
	SNT_ETH("SNT", "ETH", 38),
	
	// Basic Attention Token
	BAT_USD("BAT", "USD", 12),
	BAT_BTC("BAT", "BTC", 12),
	BAT_ETH("BAT", "ETH", 12),
	
	// Golem
	GNT_USD("GNT", "USD", 14),
	GNT_BTC("GNT", "BTC", 14),
	GNT_ETH("GNT", "ETH", 14),
	
	// SpankChain
	SPK_USD("SPK", "USD", 26);
	SPK_BTC("SPK", "BTC", 26);
	SPK_ETH("SPK", "ETH", 26);

	/**
	 * The name of the first currency 
	 */
	protected final String currency1;
	
	/**
	 * The name of the second currency
	 */
	protected final String currency2;
	
	/**
	 * The minimal order size
	 */
	protected final double minimalOrderSize;

	private BitfinexCurrencyPair(final String pair1, final String pair2, final double minimalOrderSize) {
		this.currency1 = pair1;
		this.currency2 = pair2;
		this.minimalOrderSize = minimalOrderSize;
	}
	
	public String toBitfinexString() {
		return "t" + currency1 + currency2;
	}
	
	public double getMinimalOrderSize() {
		return minimalOrderSize;
	}
	
	public static BitfinexCurrencyPair fromSymbolString(final String symbolString) {
		for (BitfinexCurrencyPair curency : BitfinexCurrencyPair.values()) {
			if (curency.toBitfinexString().equalsIgnoreCase(symbolString)) {
				return curency;
			}
		}
		throw new IllegalArgumentException("Unable to find currency pair for: " + symbolString);
	}

	public String getCurrency1() {
		return currency1;
	}
	
	public String getCurrency2() {
		return currency2;
	}
}
