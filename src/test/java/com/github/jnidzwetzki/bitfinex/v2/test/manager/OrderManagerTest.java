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
package com.github.jnidzwetzki.bitfinex.v2.test.manager;

import java.util.function.Consumer;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.OrderHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrderStatus;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;


public class OrderManagerTest {

    /**
     * Test order submit failed
     *
     * @throws APIException
     * @throws InterruptedException
     */
    @Test
    public void testOrderSubmissionFailed() throws APIException, InterruptedException {
        final String jsonString = "[0,\"n\",[null,\"on-req\",null,null,[null,null,1513970684865000,\"tBTCUSD\",null,null,0.001,0.001,\"EXCHANGE MARKET\",null,null,null,null,null,null,null,12940,null,null,null,null,null,null,0,null,null],null,\"ERROR\",\"Invalid order: minimum size for BTC/USD is 0.002\"]]";
        final JSONArray jsonArray = new JSONArray(jsonString);

        final Consumer<BitfinexSubmittedOrder> orderCallback = (e) -> {
            Assert.assertEquals(BitfinexSubmittedOrderStatus.ERROR, e.getStatus());
            Assert.assertEquals(TestHelper.API_KEY, e.getApiKey());
            Assert.assertEquals(1513970684865000L, (long)e.getClientId());
            Assert.assertEquals(BitfinexCurrencyPair.of("BTC", "USD").toBitfinexString(), e.getSymbol().toBitfinexString());
        };

        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        bitfinexApiBroker.getOrderManager().registerCallback(orderCallback);
        final NotificationHandler notificationHandler = new NotificationHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
        notificationHandler.onOrderNotification((a, eo) -> {
            bitfinexApiBroker.getOrderManager().updateOrder(a, eo);
        });

        notificationHandler.handleChannelData(null, jsonArray);
    }


    /**
     * Test notifications with null value
     *
     * @throws APIException
     * @throws InterruptedException
     */
    @Test
    public void testNotificationWithNull() throws APIException, InterruptedException {
        final String jsonString = "[0,\"n\",[1523930407542,\"on-req\",null,null,[null,null,1523930407442000,null,null,null,0.0001,null,\"LIMIT\",null,null,null,null,null,null,null,6800,null,null,null,null,null,null,0,0,null,null,null,null,null,null,null],null,\"ERROR\",\"amount: invalid\"]]";
        final JSONArray jsonArray = new JSONArray(jsonString);

        final Consumer<BitfinexSubmittedOrder> orderCallback = (e) -> {
            Assert.assertEquals(BitfinexSubmittedOrderStatus.ERROR, e.getStatus());
            Assert.assertEquals(TestHelper.API_KEY, e.getApiKey());
            Assert.assertEquals(1523930407442000L, (long) e.getClientId());
            Assert.assertNull(e.getSymbol());
        };

        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        bitfinexApiBroker.getOrderManager().registerCallback(orderCallback);
        final NotificationHandler notificationHandler = new NotificationHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));

        notificationHandler.handleChannelData(null, jsonArray);
        notificationHandler.onOrderNotification((a, eo) -> {
            bitfinexApiBroker.getOrderManager().updateOrder(a, eo);
        });
    }

    /**
     * Test the order channel handler - single order
     *
     * @throws APIException
     */
    @Test
    public void testOrderChannelHandler1() throws APIException {
        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        final String jsonString = "[0,\"on\",[6784335053,null,1514956504945000,\"tIOTUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0]]";
        final JSONArray jsonArray = new JSONArray(jsonString);
        final OrderHandler orderHandler = new OrderHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
        orderHandler.onSubmittedOrderEvent((a, eos) -> {
            for (BitfinexSubmittedOrder exchangeOrder : eos) {
                bitfinexApiBroker.getOrderManager().updateOrder(a, exchangeOrder);
            }
        });

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        Assert.assertTrue(orderManager.getOrders().isEmpty());
        orderHandler.handleChannelData(null, jsonArray);
        Assert.assertEquals(1, orderManager.getOrders().size());

        Assert.assertEquals(BitfinexSubmittedOrderStatus.ACTIVE, orderManager.getOrders().get(0).getStatus());
    }

    /**
     * Test the order channel handler - snapshot
     *
     * @throws APIException
     */
    @Test
    public void testOrderChannelHandler2() throws APIException {
        final String jsonString = "[0,\"on\",[[6784335053,null,1514956504945000,\"tIOTUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0], [67843353243,null,1514956234945000,\"tBTCUSD\",1514956505134,1514956505164,-24.175121,-24.175121,\"EXCHANGE STOP\",null,null,null,0,\"ACTIVE\",null,null,3.84,0,null,null,null,null,null,0,0,0]]]";
        final JSONArray jsonArray = new JSONArray(jsonString);
        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        final OrderHandler orderHandler = new OrderHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
        orderHandler.onSubmittedOrderEvent((a, eos) -> {
            for (BitfinexSubmittedOrder exchangeOrder : eos) {
                bitfinexApiBroker.getOrderManager().updateOrder(a, exchangeOrder);
            }
        });

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        Assert.assertTrue(orderManager.getOrders().isEmpty());
        orderHandler.handleChannelData(null, jsonArray);
        Assert.assertEquals(2, orderManager.getOrders().size());

        Assert.assertEquals(BitfinexSubmittedOrderStatus.ACTIVE, orderManager.getOrders().get(0).getStatus());
        Assert.assertEquals(BitfinexSubmittedOrderStatus.ACTIVE, orderManager.getOrders().get(1).getStatus());

        orderManager.clear();
        Assert.assertTrue(orderManager.getOrders().isEmpty());
    }

    /**
     * Test the order channel handler - posclose order
     *
     * @throws APIException
     */
    @Test
    public void testOrderChannelHandler3() throws APIException {
        final String jsonString = "[0,\"on\",[6827301913,null,null,\"tXRPUSD\",1515069803530,1515069803530,-60,-60,\"MARKET\",null,null,null,0,\"ACTIVE (note:POSCLOSE)\",null,null,0,3.2041,null,null,null,null,null,0,0,0]]";

        final JSONArray jsonArray = new JSONArray(jsonString);
        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        final OrderHandler orderHandler = new OrderHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
        orderHandler.onSubmittedOrderEvent((a,eos) -> {
            for (BitfinexSubmittedOrder exchangeOrder : eos) {
                bitfinexApiBroker.getOrderManager().updateOrder(a, exchangeOrder);
            }
        });

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        Assert.assertTrue(orderManager.getOrders().isEmpty());
        orderHandler.handleChannelData(null, jsonArray);
        Assert.assertEquals(1, orderManager.getOrders().size());
        Assert.assertEquals(BitfinexSubmittedOrderStatus.ACTIVE, orderManager.getOrders().get(0).getStatus());
    }

    /**
     * Test the order channel handler - partFilled order
     *
     * @throws APIException
     */
    @Test
    public void testOrderChannelHandler4() throws APIException {
        final String jsonString = "[0,\"oc\",[11291120775,null,null,\"tNEOBTC\",1524661302976,1524661303001,0,-0.41291886,\"MARKET\",null,null,null,0,\"INSUFFICIENT BALANCE (G1) was: ACTIVE (note:POSCLOSE), PARTIALLY FILLED @ 0.008049(-0.41291886)\",null,null,0,0.008049,null,null,null,null,null,0,0,0,null,null,\"\",null,null,null]]";

        final JSONArray jsonArray = new JSONArray(jsonString);
        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        final OrderHandler orderHandler = new OrderHandler(0, BitfinexSymbols.account("api-key", BitfinexApiKeyPermissions.ALL_PERMISSIONS));
        orderHandler.onSubmittedOrderEvent((a, eos) -> {
            for (BitfinexSubmittedOrder exchangeOrder : eos) {
                bitfinexApiBroker.getOrderManager().updateOrder(a, exchangeOrder);
            }
        });

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        Assert.assertTrue(orderManager.getOrders().isEmpty());
        orderHandler.handleChannelData(null, jsonArray);
        Assert.assertEquals(1, orderManager.getOrders().size());
        Assert.assertEquals(BitfinexSubmittedOrderStatus.PARTIALLY_FILLED, orderManager.getOrders().get(0).getStatus());
    }

    /**
     * Test the cancelation of an order
     *
     * @throws InterruptedException
     * @throws APIException
     */
    @Test(expected = APIException.class)
    public void testCancelOrderUnauth() throws APIException, InterruptedException {

        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        Mockito.when(bitfinexApiBroker.getApiKeyPermissions()).thenReturn(BitfinexApiKeyPermissions.NO_PERMISSIONS);

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        orderManager.cancelOrderAndWaitForCompletion(12);
    }

    /**
     * Test the cancelation of an order
     *
     * @throws InterruptedException
     * @throws APIException
     */
    @Test(timeout = 60000)
    public void testCancelOrder() throws APIException, InterruptedException {
        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();
        BitfinexAccountSymbol symbol = BitfinexSymbols.account("apiKey", BitfinexApiKeyPermissions.ALL_PERMISSIONS);

        final Runnable r = () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
            final BitfinexSubmittedOrder exchangeOrder = new BitfinexSubmittedOrder();
            exchangeOrder.setOrderId(12L);
            exchangeOrder.setStatus(BitfinexSubmittedOrderStatus.CANCELED);
            orderManager.updateOrder(symbol, exchangeOrder);
        };

        // Cancel event
        (new Thread(r)).start();

        orderManager.cancelOrderAndWaitForCompletion(12);
    }

    /**
     * Test the placement of an order
     *
     * @throws InterruptedException
     * @throws APIException
     */
    @Test(expected = APIException.class)
    public void testPlaceOrderUnauth() throws APIException, InterruptedException {

        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        Mockito.when(bitfinexApiBroker.getApiKeyPermissions()).thenReturn(BitfinexApiKeyPermissions.NO_PERMISSIONS);

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();

        final BitfinexNewOrder order
                = BitfinexOrderBuilder.create(BitfinexCurrencyPair.of("BCH", "USD"), BitfinexOrderType.MARKET, 12).build();

        orderManager.placeOrderAndWaitUntilActive(order);
    }


    /**
     * Test the placement of an order
     *
     * @throws InterruptedException
     * @throws APIException
     */
    @Test(timeout = 60000)
    public void testPlaceOrder() throws APIException, InterruptedException {

        final BitfinexApiBroker bitfinexApiBroker = TestHelper.buildMockedBitfinexConnection();
        Mockito.when(bitfinexApiBroker.isAuthenticated()).thenReturn(true);

        final OrderManager orderManager = bitfinexApiBroker.getOrderManager();

        final BitfinexNewOrder order
                = BitfinexOrderBuilder.create(BitfinexCurrencyPair.of("BCH", "USD"), BitfinexOrderType.MARKET, 1).build();
        BitfinexAccountSymbol symbol = BitfinexSymbols.account("apiKey", BitfinexApiKeyPermissions.ALL_PERMISSIONS);

        final Runnable r = () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
            final BitfinexSubmittedOrder exchangeOrder = new BitfinexSubmittedOrder();
            exchangeOrder.setClientId(order.getClientId());
            exchangeOrder.setStatus(BitfinexSubmittedOrderStatus.ACTIVE);
            orderManager.updateOrder(symbol, exchangeOrder);
        };

        // Cancel event
        (new Thread(r)).start();

        orderManager.placeOrderAndWaitUntilActive(order);
    }


}
