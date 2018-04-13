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
package com.github.jnidzwetzki.bitfinex.v2;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceNumberAuditor {
	
	public enum ErrorPolicy {
		LOG_ONLY,
		RUNTIME_EXCEPTION;
	}
	
	/**
	 * The public sequence
	 */
	private long publicSequence;
	
	/**
	 * The private sequence
	 */
	private long privateSequence;
	
	/**
	 * The error policy
	 */
	private ErrorPolicy errorPolicy;
	
	/**
	 * Was an error reported?
	 */
	private boolean failed;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(SequenceNumberAuditor.class);
	
	public SequenceNumberAuditor() {
		this.errorPolicy = ErrorPolicy.LOG_ONLY;
		reset();
	}

	/**
	 * Reset the number generator
	 */
	public void reset() {
		logger.debug("Resetting sequence auditor");
		this.publicSequence = -1;
		this.privateSequence = -1;
		this.failed = false;
	}
	
	/**
	 * Audit the package
	 * @param jsonArray
	 */
	public void auditPackage(final JSONArray jsonArray) {		
		final long channelId = jsonArray.getInt(0);
		final boolean isHeartbeat = jsonArray.optString(1, "").equals("hb");
		
		// Channel 0 uses the private and public sequence, other channels use only the public sequence
		// An exception is heartbeat of channel 0, in this case, only the public sequence is used
		if(channelId == 0) {
			if(isHeartbeat) {
				checkPublicSequence(jsonArray);
			} else {
				checkPublicAndPrivateSequence(jsonArray);
			}
		} else {
			checkPublicSequence(jsonArray);
		}
	}

	/**
	 * Check the public and the private sequence
	 * @param jsonArray
	 */
	private void checkPublicAndPrivateSequence(final JSONArray jsonArray) {
		final long nextPublicSequnceNumber = jsonArray.getLong(jsonArray.length() - 2);
		final long nextPrivateSequnceNumber = jsonArray.getLong(jsonArray.length() - 1);

		auditPublicSequence(nextPublicSequnceNumber);
		auditPrivateSequence(nextPrivateSequnceNumber);
	}

	/** 
	 * Check the public sequence
	 */
	private void checkPublicSequence(final JSONArray jsonArray) {
		final long nextPublicSequnceNumber = jsonArray.getLong(jsonArray.length() - 1);
		
		auditPublicSequence(nextPublicSequnceNumber);
	}

	/**
	 * Audit the public sequence
	 * 
	 * @param nextPublicSequnceNumber
	 */
	private void auditPublicSequence(final long nextPublicSequnceNumber) {
		if(publicSequence == -1) {
			publicSequence = nextPublicSequnceNumber;
			return;
		}
		
		if(publicSequence + 1 != nextPublicSequnceNumber) {
			final String errorMessage = String.format(
					"Got %d as next public sequence number, expected %d", 
					publicSequence + 1, nextPublicSequnceNumber);
			
			handleError(errorMessage);
			return;
		}
		
		publicSequence++;
	}

	/**
	 * Audit the private sequence
	 * 
	 * @param nextPublicSequnceNumber
	 */
	private void auditPrivateSequence(final long nextPrivateSequnceNumber) {
		if(privateSequence == -1) {
			privateSequence = nextPrivateSequnceNumber;
			return;
		}
		
		if(privateSequence + 1 != nextPrivateSequnceNumber) {
			final String errorMessage = String.format(
					"Got %d as next private sequence number, expected %d", 
					privateSequence + 1, nextPrivateSequnceNumber);
			
			handleError(errorMessage);
			return;
		}
		
		privateSequence++;
	}
	
	/**
	 * Handle the sequence number error
	 * @param errorMessage
	 */
	private void handleError(final String errorMessage) {
		
		failed = true;
		
		switch (errorPolicy) {
		case LOG_ONLY:
			logger.error(errorMessage);
			break;
			
		case RUNTIME_EXCEPTION:
			throw new RuntimeException(errorMessage);

		default:
			logger.error("Got error {} but unkown error policy {}", errorMessage, errorPolicy);
			break;
		}
	}
	
	/**
	 * Get the last private sequence
	 * @return
	 */
	public long getPrivateSequence() {
		return privateSequence;
	}
	
	/**
	 * Get the last public sequence
	 * @return
	 */
	public long getPublicSequence() {
		return publicSequence;
	}
	
	/**
	 * Get the error policy
	 * @return
	 */
	public ErrorPolicy getErrorPolicy() {
		return errorPolicy;
	}
	
	/**
	 * Set the error policy
	 */
	public void setErrorPolicy(final ErrorPolicy errorPolicy) {
		this.errorPolicy = errorPolicy;
	}
	
	/**
	 * Has the audit failed?
	 * @return
	 */
	public boolean isFailed() {
		return failed;
	}
}
