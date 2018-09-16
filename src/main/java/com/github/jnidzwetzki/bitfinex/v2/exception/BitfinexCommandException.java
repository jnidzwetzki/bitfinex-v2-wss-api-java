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
package com.github.jnidzwetzki.bitfinex.v2.exception;

public class BitfinexCommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2320930066822999221L;

	public BitfinexCommandException() {
	}

	public BitfinexCommandException(final String message) {
		super(message);
	}

	public BitfinexCommandException(final Throwable cause) {
		super(cause);
	}

	public BitfinexCommandException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public BitfinexCommandException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
