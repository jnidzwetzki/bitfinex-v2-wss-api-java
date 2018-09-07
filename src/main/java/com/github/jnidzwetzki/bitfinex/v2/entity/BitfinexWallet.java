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
import java.util.Objects;

public class BitfinexWallet {

	public enum Type {
		EXCHANGE("exchange"),
		MARGIN("margin"),
		FUNDING("funding");

		private final String type;

		Type(String type) {
			this.type = type;
		}

		static Type findValue(String value) {
			for (Type type : values()) {
				if (Objects.equals(type.type, value)) {
					return type;
				}
			}
			throw new IllegalArgumentException("Type not handled: " + value);
		}
	}

	private final Type walletType;
	private final String currency;
	private final BigDecimal balance;
	private final BigDecimal unsettledInterest;
	private final BigDecimal balanceAvailable;

	public BitfinexWallet(final String walletType, final String currency, BigDecimal balance,
						  BigDecimal unsettledInterest, BigDecimal balanceAvailable) {
		this.walletType = Type.findValue(walletType);
		this.currency = currency;
		this.balance = balance;
		this.unsettledInterest = unsettledInterest;
		this.balanceAvailable = balanceAvailable;
	}

	@Override
	public String toString() {
		return "BitfinexWallet [walletType=" + walletType + ", currency=" + currency + ", balance=" + balance
				+ ", unsettledInterest=" + unsettledInterest + ", balanceAvailable=" + balanceAvailable + "]";
	}

	public Type getWalletType() {
		return walletType;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}
	
	public BigDecimal getUnsettledInterest() {
		return unsettledInterest;
	}
	
	public BigDecimal getBalanceAvailable() {
		return balanceAvailable;
	}

}
