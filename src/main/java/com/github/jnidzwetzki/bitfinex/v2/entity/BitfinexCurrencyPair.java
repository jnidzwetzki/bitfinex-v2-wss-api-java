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

public enum BitfinexCurrencyPair {
	
	BTC_USD("BTC", "USD", 0.002),
	BTC_JPY("BTC", "JPY", 0.002),
	BTC_EUR("BTC", "EUR", 0.002),
	BTC_GBP("BTC", "GBP", 0.002),
	LTC_USD("LTC", "USD", 0.08),
	LTC_BTC("LTC", "BTC", 0.08),
	ETH_USD("ETH", "USD", 0.02),
	ETH_BTC("ETH", "BTC", 0.02),
	ETH_JPY("ETH", "JPY", 0.002),
	ETH_EUR("ETH", "EUR", 0.002),
	ETH_GBP("ETH", "GBP", 0.002),
	ETC_BTC("ETC", "BTC", 0.6),
	ETC_USD("ETC", "USD", 0.6),
	RRT_USD("RRT", "USD", 80.0),
	RRT_BTC("RRT", "BTC", 80.0),
	ZEC_USD("ZEC", "USD", 0.04),
	ZEC_BTC("ZEC", "BTC", 0.04),
	XMR_USD("XMR", "USD", 0.06),
	XMR_BTC("XMR", "BTC", 0.06),
	DSH_USD("DSH", "USD", 0.02),
	DSH_BTC("DSH", "BTC", 0.02),
	XRP_USD("XRP", "USD", 10.0),
	XRP_BTC("XRP", "BTC", 10.0),
	IOT_USD("IOT", "USD", 6.0),
	IOT_BTC("IOT", "BTC", 6.0),
	IOT_ETH("IOT", "ETH", 6.0),
	IOT_JPY("IOT", "JPY", 6.0),
	IOT_EUR("IOT", "EUR", 6.0),
	IOT_GBP("IOT", "GBP", 6.0),
	EOS_USD("EOS", "USD", 2.0),
	EOS_BTC("EOS", "BTC", 2.0),
	EOS_ETH("EOS", "ETH", 2.0),
	EOS_JPY("EOS", "JPY", 2.0),
	EOS_EUR("EOS", "EUR", 2.0),
	EOS_GBP("EOS", "GBP", 2.0),
	SAN_USD("SAN", "USD", 4.0),
	SAN_BTC("SAN", "BTC", 4.0),
	SAN_ETH("SAN", "ETH", 4.0),
	OMG_USD("OMG", "USD", 1.0),
	OMG_BTC("OMG", "BTC", 1.0),
	OMG_ETH("OMG", "ETH", 1.0),
	BCH_USD("BCH", "USD", 0.006),
	BCH_BTC("BCH", "BTC", 0.006),
	BCH_ETH("BCH", "ETH", 0.006),
	NEO_USD("NEO", "USD", 0.2),
	NEO_BTC("NEO", "BTC", 0.2),
	NEO_ETH("NEO", "ETH", 0.2),
	NEO_JPY("NEO", "JPY", 0.2),
	NEO_EUR("NEO", "EUR", 0.2),
	NEO_GBP("NEO", "GBP", 0.2),
	ETP_USD("ETP", "USD", 4.0),
	ETP_BTC("ETP", "BTC", 4.0),
	ETP_ETH("ETP", "ETH", 4.0),
	QTM_USD("QTM", "USD", 0.4),
	QTM_BTC("QTM", "BTC", 0.4),
	QTM_ETH("QTM", "ETH", 0.4),
	AVT_USD("AVT", "USD", 4.0),
	AVT_BTC("AVT", "BTC", 4.0),
	AVT_ETH("AVT", "ETH", 4.0),
	EDO_USD("EDO", "USD", 4.0),
	EDO_BTC("EDO", "BTC", 4.0),
	EDO_ETH("EDO", "ETH", 4.0),
	BTG_USD("BTG", "USD", 0.06),
	BTG_BTC("BTG", "BTC", 0.06),
	DAT_USD("DAT", "USD", 74.0),
	DAT_BTC("DAT", "BTC", 74.0),
	DAT_ETH("DAT", "ETH", 74.0),
	QSH_USD("QSH", "USD", 10.0),
	QSH_BTC("QSH", "BTC", 10.0),
	QSH_ETH("QSH", "ETH", 10.0),
	YYW_USD("YYW", "USD", 48.0),
	YYW_BTC("YYW", "BTC", 48.0),
	YYW_ETH("YYW", "ETH", 48.0),
	GNT_USD("GNT", "USD", 16.0),
	GNT_BTC("GNT", "BTC", 16.0),
	GNT_ETH("GNT", "ETH", 16.0),
	SNT_USD("SNT", "USD", 38.0),
	SNT_BTC("SNT", "BTC", 38.0),
	SNT_ETH("SNT", "ETH", 38.0),
	BAT_USD("BAT", "USD", 18.0),
	BAT_BTC("BAT", "BTC", 18.0),
	BAT_ETH("BAT", "ETH", 18.0),
	MNA_USD("MNA", "USD", 80.0),
	MNA_BTC("MNA", "BTC", 80.0),
	MNA_ETH("MNA", "ETH", 80.0),
	FUN_USD("FUN", "USD", 108.0),
	FUN_BTC("FUN", "BTC", 108.0),
	FUN_ETH("FUN", "ETH", 108.0),
	ZRX_USD("ZRX", "USD", 6.0),
	ZRX_BTC("ZRX", "BTC", 6.0),
	ZRX_ETH("ZRX", "ETH", 6.0),
	TNB_USD("TNB", "USD", 104.0),
	TNB_BTC("TNB", "BTC", 104.0),
	TNB_ETH("TNB", "ETH", 104.0),
	SPK_USD("SPK", "USD", 26.0),
	SPK_BTC("SPK", "BTC", 26.0),
	SPK_ETH("SPK", "ETH", 26.0),
	TRX_BTC("TRX", "BTC", 26.0),
	TRX_ETH("TRX", "ETH", 26.0),
	TRX_USD("TRX", "USD", 26.0),
	RCN_BTC("RCN", "BTC", 26.0),
	RCN_ETH("RCN", "ETH", 26.0),
	RCN_USD("RCN", "USD", 26.0),
	RLC_BTC("RLC", "BTC", 26.0),
	RLC_ETH("RLC", "ETH", 26.0),
	RLC_USD("RLC", "USD", 26.0),
	AID_BTC("AID", "BTC", 26.0),
	AID_USD("AID", "USD", 26.0),
	AID_ETH("AID", "ETH", 26.0),
	SNG_BTC("SNG", "BTC", 26.0),
	SNG_ETH("SNG", "ETH", 26.0),
	SNG_USD("SNG", "USD", 26.0),
	REP_BTC("REP", "BTC", 26.0),
	REP_ETH("REP", "ETH", 26.0),
	REP_USD("REP", "USD", 26.0),
	ELF_USD("ELF", "USD", 26.0),
	ELF_BTC("ELF", "BTC", 26.0),
	ELF_ETH("ELF", "ETH", 26.0),
	BFT_USD("BFT", "USD", 26.0),
	BFT_BTC("BFT", "BTC", 26.0),
	BFT_ETH("BFT", "ETH", 26.0),
	IOS_USD("IOS", "USD", 26.0),
	IOS_BTC("IOS", "BTC", 26.0),
	IOS_ETH("IOS", "ETH", 26.0),
	AIO_USD("AIO", "USD", 26.0),
	AIO_BTC("AIO", "BTC", 26.0),
	AIO_ETH("AIO", "ETH", 26.0),
	REQ_USD("REQ", "USD", 26.0),
	REQ_BTC("REQ", "BTC", 26.0),
	REQ_ETH("REQ", "ETH", 26.0),
	RDN_USD("RDN", "USD", 26.0),
	RDN_BTC("RDN", "BTC", 26.0),
	RDN_ETH("RDN", "ETH", 26.0),
	LRC_USD("LRC", "USD", 26.0),
	LRC_BTC("LRC", "BTC", 26.0),
	LRC_ETH("LRC", "ETH", 26.0),
	WAX_USD("WAX", "USD", 26.0),
	WAX_BTC("WAX", "BTC", 26.0),
	WAX_ETH("WAX", "ETH", 26.0),
	DAI_USD("DAI", "USD", 26.0),
	DAI_BTC("DAI", "BTC", 26.0),
	DAI_ETH("DAI", "ETH", 26.0),
	CFI_USD("CFI", "USD", 26.0),
	CFI_BTC("CFI", "BTC", 26.0),
	CFI_ETH("CFI", "ETH", 26.0),
	AGI_USD("AGI", "USD", 26.0),
	AGI_BTC("AGI", "BTC", 26.0),
	AGI_ETH("AGI", "ETH", 26.0),
	MTN_USD("MTN", "USD", 26.0),
	MTN_BTC("MTN", "BTC", 26.0),
	MTN_ETH("MTN", "ETH", 26.0),
	ODE_USD("ODE", "USD", 26.0),
	ODE_BTC("ODE", "BTC", 26.0),
	ODE_ETH("ODE", "ETH", 26.0);


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
		for (BitfinexCurrencyPair curency : BitfinexCurrencyPair.values()) {
			if (curency.toBitfinexString().equalsIgnoreCase(symbolString)) {
				return curency;
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
