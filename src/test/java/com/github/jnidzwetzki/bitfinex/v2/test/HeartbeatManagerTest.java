package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.HeartbeatThread;
import com.github.jnidzwetzki.bitfinex.v2.WebsocketClientEndpoint;


public class HeartbeatManagerTest {

	@Test(timeout=30000)
	public void testConnectWhenDisconnected() throws Exception {
		
		// Events
		// * Disconnect websocket from heartbeat
		// * Disconnect from bitfinex API reconnect method
		// * Reconnect on bitfinex api
		final CountDownLatch connectLatch = new CountDownLatch(3);
		
		// Count down the latch on method call
		final Answer<Void> answer = new Answer<Void>() {
	        public Void answer(final InvocationOnMock invocation) throws Throwable {
	        		connectLatch.countDown();
	        		return null;
	        }
		};
		
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);

		final HeartbeatThread heartbeatThreadRunnable = new HeartbeatThread(bitfinexApiBroker);
		final WebsocketClientEndpoint websocketClientEndpoint = Mockito.mock(WebsocketClientEndpoint.class);
		Mockito.when(websocketClientEndpoint.isConnected()).thenReturn(connectLatch.getCount() == 0);
		Mockito.when(bitfinexApiBroker.getWebsocketEndpoint()).thenReturn(websocketClientEndpoint);

		Mockito.doAnswer(answer).when(bitfinexApiBroker).reconnect();
		Mockito.doAnswer(answer).when(websocketClientEndpoint).close();
		
		final Thread heartbeatThread = new Thread(heartbeatThreadRunnable);
		
		try {
			heartbeatThread.start();
			connectLatch.await();
		} catch (Exception e) {
			// Should not happen
			throw e;
		} finally {
			heartbeatThread.interrupt();
		}
	}
}
