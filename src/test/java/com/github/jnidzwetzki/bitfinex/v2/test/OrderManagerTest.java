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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.api.OrderHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;
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
	 * Test the order channel handler - single order
	 * @throws APIException 
	 */
	@Test
	public void testOrderChannelHandler1() throws APIException {
		final String jsonString = "[0,\"on\",[6784335053,null,1514956504945000,\"tIOTUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		final OrderHandler orderHandler = new OrderHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		Assert.assertTrue(orderManager.getOrders().isEmpty());
		orderHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		Assert.assertEquals(1, orderManager.getOrders().size());	
	}
	
	/**
	 * Test the order channel handler - snapshot
	 * @throws APIException 
	 */
	@Test
	public void testOrderChannelHandler2() throws APIException {
		final String jsonString = "[0,\"on\",[[6784335053,null,1514956504945000,\"tIOTUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0], [67843353243,null,1514956234945000,\"tBTCUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0]]]";
		final JSONArray jsonArray = new JSONArray(jsonString);
		final OrderHandler orderHandler = new OrderHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		Assert.assertTrue(orderManager.getOrders().isEmpty());
		orderHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		Assert.assertEquals(2, orderManager.getOrders().size());	
		
		orderManager.clear();
		Assert.assertTrue(orderManager.getOrders().isEmpty());
	}
	
	/**
	 * Test the order channel handler - posclose order
	 * @throws APIException 
	 */
	@Test
	public void testOrderChannelHandler3() throws APIException {
		final String jsonString = "[0,\"on\",[6827301913,null,null,\"tXRPUSD\",1515069803530,1515069803530,-60,-60,\"MARKET\",null,null,null,0,\"ACTIVE (note:POSCLOSE)\",null,null,0,3.2041,null,null,null,null,null,0,0,0]]";
		
		final JSONArray jsonArray = new JSONArray(jsonString);
		final OrderHandler orderHandler = new OrderHandler();
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		Assert.assertTrue(orderManager.getOrders().isEmpty());
		orderHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		Assert.assertEquals(1, orderManager.getOrders().size());	
	}
	
	/**
	 * Test the cancelation of an order
	 * @throws InterruptedException 
	 * @throws APIException 
	 */
	@Test(expected=APIException.class)
	public void testCancelOrderUnauth() throws APIException, InterruptedException {
		
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		Mockito.when(bitfinexApiBroker.getCapabilities()).thenReturn(ConnectionCapabilities.NO_CAPABILITIES);

		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		orderManager.cancelOrderAndWaitForCompletion(12);
	}
	
	/**
	 * Test the cancelation of an order
	 * @throws InterruptedException 
	 * @throws APIException 
	 */
	@Test(timeout=60000)
	public void testCancelOrder() throws APIException, InterruptedException {
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		
		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		
		final Runnable r = () -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
			final ExchangeOrder exchangeOrder = new ExchangeOrder();
			exchangeOrder.setOrderId(12);
			exchangeOrder.setState(ExchangeOrderState.STATE_CANCELED);
			orderManager.updateOrder(exchangeOrder);
		};
		
		// Cancel event
		(new Thread(r)).start();
		
		orderManager.cancelOrderAndWaitForCompletion(12);
	}
	
	/**
	 * Test the placement of an order
	 * @throws InterruptedException 
	 * @throws APIException 
	 */
	@Test(expected=APIException.class)
	public void testPlaceOrderUnauth() throws APIException, InterruptedException {
		
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		Mockito.when(bitfinexApiBroker.getCapabilities()).thenReturn(ConnectionCapabilities.NO_CAPABILITIES);

		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		
		final BitfinexOrder order 
			= BitfinexOrderBuilder.create(BitfinexCurrencyPair.BCH_USD, BitfinexOrderType.MARKET, 12).build();
		
		orderManager.placeOrderAndWaitUntilActive(order);
	}

	
	/**
	 * Test the placement of an order
	 * @throws InterruptedException 
	 * @throws APIException 
	 */
	@Test(timeout=60000)
	public void testPlaceOrder() throws APIException, InterruptedException {
		
		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();
		Mockito.when(bitfinexApiBroker.isAuthenticated()).thenReturn(true);
		
		final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
		
		final BitfinexOrder order 
			= BitfinexOrderBuilder.create(BitfinexCurrencyPair.BCH_USD, BitfinexOrderType.MARKET, 1).build();
	
		final Runnable r = () -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
			final ExchangeOrder exchangeOrder = new ExchangeOrder();
			exchangeOrder.setCid(order.getCid());
			exchangeOrder.setState(ExchangeOrderState.STATE_ACTIVE);
			orderManager.updateOrder(exchangeOrder);
		};
		
		// Cancel event
		(new Thread(r)).start();
	
		orderManager.placeOrderAndWaitUntilActive(order);
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
		Mockito.when(bitfinexApiBroker.isAuthenticated()).thenReturn(true);
		Mockito.when(bitfinexApiBroker.getCapabilities()).thenReturn(ConnectionCapabilities.ALL_CAPABILITIES);
		
		final OrderManager orderManager = new OrderManager(bitfinexApiBroker);
		Mockito.when(bitfinexApiBroker.getOrderManager()).thenReturn(orderManager);
		
		return bitfinexApiBroker;
	}
	
}
