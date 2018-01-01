package com.github.jnidzwetzki.bitfinex.v2.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;

public class IntegrationTest {
	
	/**
	 * Try to fetch wallets on an unauthenticated connection
	 */
	@Test
	public void testWalletsOnUnauthClient() throws APIException {
		
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();

		try {
			bitfinexClient.connect();
			Assert.assertFalse(bitfinexClient.isAuthenticated());
			
			try {
				bitfinexClient.getWallets();

				// Should not happen
				Assert.assertTrue(false);
			} catch (APIException e) {
				return;
			}
		
		} catch (Exception e) {
			
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
}
