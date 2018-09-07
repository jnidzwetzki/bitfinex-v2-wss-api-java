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
package com.github.jnidzwetzki.bitfinex.v2.commands;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.io.BaseEncoding;
import org.json.JSONObject;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.exception.CommandException;

public class AuthCommand extends AbstractAPICommand {

	/**
	 * The used auth algorithm
	 */
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA384";
	
	/**
	 * The auth nonce supplier
	 */
	private final Supplier<String> authNonceSupplier;
	
	/**
	 * Default auth nonce producer
	 */
	public static Supplier<String> AUTH_NONCE_PRODUCER_TIMESTAMP = () -> Long.toString(System.currentTimeMillis());
	
	public AuthCommand(final Supplier<String> authNonceSupplier) {
		this.authNonceSupplier = Objects.requireNonNull(authNonceSupplier);
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException {
		try {
			final String APIKey = bitfinexApiBroker.getConfiguration().getApiKey();
			final String APISecret = bitfinexApiBroker.getConfiguration().getApiSecret();
			final boolean deadManSwitch = bitfinexApiBroker.getConfiguration().isDeadmanSwitchActive();
			
			final String authNonce = authNonceSupplier.get();
			final String authPayload = "AUTH" + authNonce;

			final SecretKeySpec signingKey = new SecretKeySpec(APISecret.getBytes(), HMAC_SHA1_ALGORITHM);
			final Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			
			final byte[] encodedBytes = mac.doFinal(authPayload.getBytes());		
			final String authSig = BaseEncoding.base16().encode(encodedBytes);
			
			final JSONObject subscribeJson = new JSONObject();
			subscribeJson.put("event", "auth");
			subscribeJson.put("apiKey", APIKey);
			subscribeJson.put("authSig", authSig.toLowerCase());
			subscribeJson.put("authPayload", authPayload);
			subscribeJson.put("authNonce", authNonce);
			
			if(deadManSwitch) {
				subscribeJson.put("dms", 4);
			}
			
			return subscribeJson.toString();
		} catch (Exception e) {
			throw new CommandException(e);
		} 
	}

}
