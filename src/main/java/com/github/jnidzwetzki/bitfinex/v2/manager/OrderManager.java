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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bboxdb.commons.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.command.CancelOrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.CancelOrderGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;

public class OrderManager extends SimpleCallbackManager<BitfinexSubmittedOrder> {

	/**
	 * The orders
	 */
	private final List<BitfinexSubmittedOrder> orders;

	/**
	 * The order timeout
	 */
	private final long TIMEOUT_IN_SECONDS = 120;

	/**
	 * The number of order retries on error
	 */
	private static final int ORDER_RETRIES = 3;

	/**
	 * The delay between two retries
	 */
	private static final int RETRY_DELAY_IN_MS = 1000;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(OrderManager.class);


	public OrderManager(final BitfinexApiBroker bitfinexApiBroker, final ExecutorService executorService) {
		super(executorService, bitfinexApiBroker);
		this.orders = new ArrayList<>();
		bitfinexApiBroker.getCallbacks().onSubmittedOrderEvent((a, e) -> e.forEach(i -> updateOrder(a, i)));
		bitfinexApiBroker.getCallbacks().onOrderNotification(this::updateOrder);
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
	public List<BitfinexSubmittedOrder> getOrders() throws APIException {
		synchronized (orders) {
			return orders;
		}
	}

	/**
	 * Update a exchange order
	 * @param exchangeOrder
	 */
	public void updateOrder(final BitfinexAccountSymbol account, final BitfinexSubmittedOrder exchangeOrder) {

		synchronized (orders) {
			// Replace order
			orders.removeIf(o -> Objects.equals(o.getOrderId(), exchangeOrder.getOrderId()));

			// Remove canceled orders
			if(exchangeOrder.getStatus() != BitfinexSubmittedOrderStatus.CANCELED) {
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
	public void placeOrderAndWaitUntilActive(final BitfinexNewOrder order) throws APIException, InterruptedException {

		final BitfinexApiKeyPermissions capabilities = bitfinexApiBroker.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new APIException("Unable to wait for order " + order + " connection has not enough capabilities: " + capabilities);
		}

		order.setApiKey(bitfinexApiBroker.getConfiguration().getApiKey());

		final Callable<Boolean> orderCallable = () -> placeOrderOrderOnAPI(order);

		// Bitfinex does not implement a happens-before relationship. Sometimes
		// canceling a stop-loss order and placing a new stop-loss order results
		// in an 'ERROR, reason is Invalid order: not enough exchange balance'
		// error for some seconds. The retryer tries to place the order up to
		// three times
		final Retryer<Boolean> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS,
				TimeUnit.MILLISECONDS, orderCallable);
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
	private boolean placeOrderOrderOnAPI(final BitfinexNewOrder order) throws Exception {
		final CountDownLatch waitLatch = new CountDownLatch(1);

		final Consumer<BitfinexSubmittedOrder> ordercallback = (o) -> {
			if(Objects.equals(o.getClientId(), order.getClientId())) {
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
					.filter(o -> o.getClientId() == order.getClientId())
					.anyMatch(o -> o.getStatus() == BitfinexSubmittedOrderStatus.ERROR);

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

		final BitfinexApiKeyPermissions capabilities = bitfinexApiBroker.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new APIException("Unable to cancel order " + id + " connection has not enough capabilities: " + capabilities);
		}

		final Callable<Boolean> orderCallable = () -> cancelOrderOnAPI(id);

		// See comment in placeOrder()
		final Retryer<Boolean> retryer = new Retryer<>(ORDER_RETRIES, RETRY_DELAY_IN_MS,
				TimeUnit.MILLISECONDS, orderCallable);
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

		final Consumer<BitfinexSubmittedOrder> ordercallback = (o) -> {
			if(o.getOrderId() == id && o.getStatus() == BitfinexSubmittedOrderStatus.CANCELED) {
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
	public void placeOrder(final BitfinexNewOrder order) throws APIException {

		final BitfinexApiKeyPermissions capabilities = bitfinexApiBroker.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new APIException("Unable to place order " + order + " connection has not enough capabilities: " + capabilities);
		}

		logger.info("Executing new order {}", order);
		final OrderCommand orderCommand = new OrderCommand(order);
		bitfinexApiBroker.sendCommand(orderCommand);
	}

	/**
	 * Cancel the given order
	 * @param id
	 * @throws APIException
	 */
	public void cancelOrder(final long id) throws APIException {

		final BitfinexApiKeyPermissions capabilities = bitfinexApiBroker.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new APIException("Unable to cancel order " + id + " connection has not enough capabilities: " + capabilities);
		}

		logger.info("Cancel order with id {}", id);
		final CancelOrderCommand cancelOrder = new CancelOrderCommand(id);
		bitfinexApiBroker.sendCommand(cancelOrder);
	}

	/**
	 * Cancel the given order group
	 * @param id
	 * @throws APIException
	 */
	public void cancelOrderGroup(final int id) throws APIException {

		final BitfinexApiKeyPermissions capabilities = bitfinexApiBroker.getApiKeyPermissions();

		if(! capabilities.isOrderWritePermission()) {
			throw new APIException("Unable to cancel order group " + id + " connection has not enough capabilities: " + capabilities);
		}

		logger.info("Cancel order group {}", id);
		final CancelOrderGroupCommand cancelOrder = new CancelOrderGroupCommand(id);
		bitfinexApiBroker.sendCommand(cancelOrder);
	}
}
