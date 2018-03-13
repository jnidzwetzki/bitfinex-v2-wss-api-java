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
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bboxdb.commons.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.commands.CancelOrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.CancelOrderGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.commands.OrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ConnectionCapabilities;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class OrderManager extends SimpleCallbackManager<ExchangeOrder> {

	/**
	 * The orders
	 */
	private final List<ExchangeOrder> orders;
	
	/**
	 * The api broker
	 */
	private BitfinexApiBroker bitfinexApiBroker;

	/**
	 * The order timeout
	 */
	private final long TIMEOUT_IN_SECONDS = 120;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(OrderManager.class);

	/**
	 * The number of order retries on error
	 */
	private static final int ORDER_RETRIES = 3;

	/**
	 * The delay between two retries
	 */
	private static final int RETRY_DELAY_IN_MS = 1000;

	public OrderManager(final BitfinexApiBroker bitfinexApiBroker) {
		super(bitfinexApiBroker.getExecutorService());
		this.bitfinexApiBroker = bitfinexApiBroker;
		this.orders = new ArrayList<>();
	}
	
	/**
	 * Clear all orders
	 */
	public void clear() {
		synchronized (orders) {
			orders.clear();	
		}
	}

	/**
	 * Get the list with exchange orders
	 * @return
	 * @throws APIException 
	 */
	public List<ExchangeOrder> getOrders() throws APIException {		
		synchronized (orders) {
			return orders;
		}
	}
	
	/**
	 * Update a exchange order
	 * @param exchangeOrder
	 */
	public void updateOrder(final ExchangeOrder exchangeOrder) {
		
		synchronized (orders) {
			// Replace order 
			orders.removeIf(o -> o.getOrderId() == exchangeOrder.getOrderId());
			
			// Remove canceled orders
			if(exchangeOrder.getState() != ExchangeOrderState.STATE_CANCELED) {
				orders.add(exchangeOrder);
			}
						
			orders.notifyAll();
		}
		
		notifyCallbacks(exchangeOrder);
	}


	/**
	 * Place an order and retry if Exception occur
	 * @param order - new BitfinexOrder to place
	 * @throws APIException
	 * @throws InterruptedException
	 */
	public void placeOrderAndWaitUntilActive(final BitfinexOrder order) throws APIException, InterruptedException {
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();
		
		if(! capabilities.isHavingOrdersWriteCapability()) {
			throw new APIException("Unable to wait for order " + order + " connection has not enough capabilities: " + capabilities);
		}
		
		order.setApikey(bitfinexApiBroker.getApiKey());
		
		final Callable<Boolean> orderCallable = () -> placeOrderOrderOnAPI(order);
		
		// Bitfinex does not implement a happens-before relationship. Sometimes
		// canceling a stop-loss order and placing a new stop-loss order results 
		// in an 'ERROR, reason is Invalid order: not enough exchange balance' 
		// error for some seconds. The retryer tries to place the order up to 
		// three times
		final Retryer<Boolean> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS, orderCallable);
		retryer.execute();
		
		if(retryer.getNeededExecutions() > 1) {
			logger.info("Nedded {} executions for placing the order", retryer.getNeededExecutions());
		}
		
		if(! retryer.isSuccessfully()) {
			final Exception lastException = retryer.getLastException();
			
			if(lastException == null) {
				throw new APIException("Unable to execute order");
			} else {
				throw new APIException(lastException);
			}
		}
	}

	/**
	 * Execute a new Order
	 * @param order
	 * @return 
	 * @throws Exception
	 */
	private boolean placeOrderOrderOnAPI(final BitfinexOrder order) throws Exception {
		final CountDownLatch waitLatch = new CountDownLatch(1);
		
		final Consumer<ExchangeOrder> ordercallback = (o) -> {
			if(o.getCid() == order.getCid()) {
				waitLatch.countDown();
			}
		};
		
		registerCallback(ordercallback);
		
		try {
			placeOrder(order);
			
			waitLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

			if(waitLatch.getCount() != 0) {
				throw new APIException("Timeout while waiting for order");
			}
			
			// Check for order error
			final boolean orderInErrorState = bitfinexApiBroker
					.getOrderManager()
					.getOrders()
					.stream()
					.filter(o -> o.getCid() == order.getCid())
					.anyMatch(o -> o.getState() == ExchangeOrderState.STATE_ERROR);
			
			if(orderInErrorState) {
				throw new APIException("Unable to place order " + order);
			}
			
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			removeCallback(ordercallback);
		}		
	}
	
	/**
	 * Cancel a order
	 * @param id
	 * @throws APIException, InterruptedException 
	 */
	public void cancelOrderAndWaitForCompletion(final long id) throws APIException, InterruptedException {
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();
		
		if(! capabilities.isHavingOrdersWriteCapability()) {
			throw new APIException("Unable to cancel order " + id + " connection has not enough capabilities: " + capabilities);
		}
		
		final Callable<Boolean> orderCallable = () -> cancelOrderOnAPI(id);
		
		// See comment in placeOrder()
		final Retryer<Boolean> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS, orderCallable);
		retryer.execute();
		
		if(retryer.getNeededExecutions() > 1) {
			logger.info("Nedded {} executions for canceling the order", retryer.getNeededExecutions());
		}
		
		if(! retryer.isSuccessfully()) {
			final Exception lastException = retryer.getLastException();
			
			if(lastException == null) {
				throw new APIException("Unable to cancel order");
			} else {
				throw new APIException(lastException);
			}
		}
	}

	/**
	 * Cancel the order on the API
	 * @param id
	 * @return
	 * @throws APIException
	 * @throws InterruptedException
	 */
	private boolean cancelOrderOnAPI(final long id) throws APIException, InterruptedException {
		final CountDownLatch waitLatch = new CountDownLatch(1);
		
		final Consumer<ExchangeOrder> ordercallback = (o) -> {
			if(o.getOrderId() == id && o.getState() == ExchangeOrderState.STATE_CANCELED) {
				waitLatch.countDown();
			}
		};
		
		registerCallback(ordercallback);
		
		try {
			logger.info("Cancel order: {}", id);
			cancelOrder(id);
			waitLatch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			
			if(waitLatch.getCount() != 0) {
				throw new APIException("Timeout while waiting for order");
			}
			
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			removeCallback(ordercallback);
		}
	}
	

	/**
	 * Place a new order
	 * @throws APIException 
	 */
	public void placeOrder(final BitfinexOrder order) throws APIException {	
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();
		
		if(! capabilities.isHavingOrdersWriteCapability()) {
			throw new APIException("Unable to place order " + order + " connection has not enough capabilities: " + capabilities);
		}
		
		logger.info("Executing new order {}", order);
		final OrderCommand orderCommand = new OrderCommand(order);
		bitfinexApiBroker.sendCommand(orderCommand);
	}
	
	/**
	 * Cancel the given order
	 * @param cid
	 * @param date
	 * @throws APIException 
	 */
	public void cancelOrder(final long id) throws APIException {	
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();
		
		if(! capabilities.isHavingOrdersWriteCapability()) {
			throw new APIException("Unable to cancel order " + id + " connection has not enough capabilities: " + capabilities);
		}
		
		logger.info("Cancel order with id {}", id);
		final CancelOrderCommand cancelOrder = new CancelOrderCommand(id);
		bitfinexApiBroker.sendCommand(cancelOrder);
	}
	
	/**
	 * Cancel the given order group
	 * @param cid
	 * @param date
	 * @throws APIException 
	 */
	public void cancelOrderGroup(final int id) throws APIException {		
		
		final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();
		
		if(! capabilities.isHavingOrdersWriteCapability()) {
			throw new APIException("Unable to cancel order group " + id + " connection has not enough capabilities: " + capabilities);
		}
		
		logger.info("Cancel order group {}", id);
		final CancelOrderGroupCommand cancelOrder = new CancelOrderGroupCommand(id);
		bitfinexApiBroker.sendCommand(cancelOrder);
	}
}
