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
package com.github.jnidzwetzki.bitfinex.v2.test;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.SequenceNumberAuditor;
import com.github.jnidzwetzki.bitfinex.v2.SequenceNumberAuditor.ErrorPolicy;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;

public class SequenceNumberAuditorTest {

	private SequenceNumberAuditor sequenceNumberAuditor;

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
		}
		BitfinexCurrencyPair.registerDefaults();	
	}

	@Before
	public void before() {
		this.sequenceNumberAuditor = new SequenceNumberAuditor();
	}

	@Test
	public void testPublicSequenceStartAt1() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		final JSONArray jsonArray1 = new JSONArray("[2,[],1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[2,[],2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[2,[],3]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPublicSequenceStartAt5() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		final JSONArray jsonArray1 = new JSONArray("[2,[],5]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[2,[],6]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[2,[],7]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPublicSequenceStartAt5WithReset() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		final JSONArray jsonArray1 = new JSONArray("[2,[],5]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[2,[],6]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[2,[],5]");
		sequenceNumberAuditor.reset();
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPublicPrivateSequenceStartAt5And1() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		final JSONArray jsonArray1 = new JSONArray("[0,[],5, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		Assert.assertEquals(5,  sequenceNumberAuditor.getPublicSequence());
		Assert.assertEquals(1,  sequenceNumberAuditor.getPrivateSequence());

		final JSONArray jsonArray2 = new JSONArray("[0,[],6, 2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,[],7, 3]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
		Assert.assertEquals(7,  sequenceNumberAuditor.getPublicSequence());
		Assert.assertEquals(3,  sequenceNumberAuditor.getPrivateSequence());
	}
	
	@Test
	public void testPublicPrivateSequenceHeartbeat() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		final JSONArray jsonArray1 = new JSONArray("[0,\"hb\", 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[0,\"hb\", 2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,\"hb\", 3]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPublicFailed() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		sequenceNumberAuditor.setErrorPolicy(ErrorPolicy.LOG_ONLY);
		final JSONArray jsonArray1 = new JSONArray("[0,\"hb\", 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[0,\"hb\", 1]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,\"hb\", 3]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());	
		
		sequenceNumberAuditor.reset();
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPrivateFailed() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		sequenceNumberAuditor.setErrorPolicy(ErrorPolicy.LOG_ONLY);
		final JSONArray jsonArray1 = new JSONArray("[0,[],5, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[0,[],5, 2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,[],5, 3]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());	
		
		sequenceNumberAuditor.reset();
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test
	public void testPublicAndPrivateFailed() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		sequenceNumberAuditor.setErrorPolicy(ErrorPolicy.LOG_ONLY);
		final JSONArray jsonArray1 = new JSONArray("[0,[],5, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[0,[],6, 2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,[],7, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());	
		
		sequenceNumberAuditor.reset();
		Assert.assertFalse(sequenceNumberAuditor.isFailed());	
	}
	
	@Test(expected=RuntimeException.class)
	public void testErrorPolicyException() {
		Assert.assertFalse(sequenceNumberAuditor.isFailed());
		
		sequenceNumberAuditor.setErrorPolicy(ErrorPolicy.RUNTIME_EXCEPTION);
		final JSONArray jsonArray1 = new JSONArray("[0,[],5, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray1);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray2 = new JSONArray("[0,[],6, 2]");
		sequenceNumberAuditor.auditPackage(jsonArray2);
		Assert.assertFalse(sequenceNumberAuditor.isFailed());

		final JSONArray jsonArray3 = new JSONArray("[0,[],7, 1]");
		sequenceNumberAuditor.auditPackage(jsonArray3);
		Assert.assertTrue(sequenceNumberAuditor.isFailed());	
	}
	
}
