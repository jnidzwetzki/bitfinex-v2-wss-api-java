package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;
import com.github.jnidzwetzki.bitfinex.v2.entity.symbol.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;


public class OrderManagerTest {

	/**
	 * The API key of the connection
	 */
	private final static String API_KEY = "abc123";
	
	/**
	 * Test order submit failed
	 * @throws APIException
	 * @throws InterruptedException
	 */
	@Test(timeout=10000)
	public void testOrderSubmissionFailed() throws APIException, InterruptedException {
		final String jsonString = "[0,\"n\",[null,\"on-req\",null,null,[null,null,1513970684865000,\"tBTCUSD\",null,null,0.001,0.001,\"EXCHANGE MARKET\",null,null,null,null,null,null,null,12940,null,null,null,null,null,null,0,null,null],null,\"ERROR\",\"Invalid order: minimum size for BTC/USD is 0.002\"]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		final Consumer<ExchangeOrder> orderCallback = (e) -> {
			
			Assert.assertEquals(ExchangeOrderState.STATE_ERROR, e.getState());
			Assert.assertEquals(API_KEY, e.getApikey());
			Assert.assertEquals(1513970684865000l, e.getCid());
			Assert.assertEquals(BitfinexCurrencyPair.BTC_USD.toBitfinexString(), e.getSymbol());
			
			latch.countDown();
		};
		
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		bitfinexApiBroker.getOrderManager().registerCallback(orderCallback);
		final NotificationHandler notificationHandler = new NotificationHandler();
		
		notificationHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		latch.await();
	}

	/**
	 * Build a mocked bitfinex connection
	 * @return
	 */
	private BitfinexApiBroker buildMockedBitfinexConnection() {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		
		Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
		Mockito.when(bitfinexApiBroker.getApiKey()).thenReturn(API_KEY);
		
		final OrderManager orderManager = new OrderManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getOrderManager()).thenReturn(orderManager);
		
		return bitfinexApiBroker;
	}
}
