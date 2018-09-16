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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.MyExecutedTradeHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.NotificationHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.OrderHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.PositionHandler;
import com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info.WalletHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexMyExecutedTrade;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexPosition;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexWallet;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public class AccountInfoHandler implements ChannelCallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(AccountInfoHandler.class);
    
    private final Map<String, ChannelCallbackHandler> channelHandler = new HashMap<>();

    private final int channelId;
    private final BitfinexAccountSymbol symbol;

    private final HeartbeatHandler heartbeatHandler;
    private final PositionHandler positionHandler;
    private final WalletHandler walletHandler;
    private final OrderHandler orderHandler;
    private final MyExecutedTradeHandler tradeHandler;
    private final NotificationHandler notificationHandler;

    public AccountInfoHandler(final int channelId, final BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;

        heartbeatHandler = new HeartbeatHandler(channelId, symbol);
        channelHandler.put("hb", heartbeatHandler);

        positionHandler = new PositionHandler(channelId, symbol);
        channelHandler.put("ps", positionHandler); // Position snapshot
        channelHandler.put("pn", positionHandler); // Position new
        channelHandler.put("pu", positionHandler); // Position updated
        channelHandler.put("pc", positionHandler); // Position canceled

        final ChannelCallbackHandler fundingHandler = new DoNothingHandler();
        channelHandler.put("fos", fundingHandler); // Founding offers
        channelHandler.put("fcs", fundingHandler); // Founding credits
        channelHandler.put("fls", fundingHandler); // Founding loans

        channelHandler.put("ats", new DoNothingHandler()); // Ats - Unknown

        walletHandler = new WalletHandler(channelId, symbol);
        channelHandler.put("ws", walletHandler); // Wallet snapshot
        channelHandler.put("wu", walletHandler); // Wallet update

        orderHandler = new OrderHandler(channelId, symbol);
        channelHandler.put("os", orderHandler); // Order snapshot
        channelHandler.put("on", orderHandler); // Order notification
        channelHandler.put("ou", orderHandler); // Order update
        channelHandler.put("oc", orderHandler); // Order cancellation

        tradeHandler = new MyExecutedTradeHandler(channelId, symbol);
        channelHandler.put("te", tradeHandler); // Trade executed
        channelHandler.put("tu", tradeHandler); // Trade updates

        notificationHandler = new NotificationHandler(channelId, symbol);
        channelHandler.put("n", notificationHandler); // General notification
    }

    @Override
    public void handleChannelData(final String action, final JSONArray message) throws BitfinexClientException {
        if (message.toString().contains("ERROR")) {
            logger.error("Got Error message: {}", message.toString());
        }
        final ChannelCallbackHandler handler = channelHandler.get(action);
        if (handler == null) {
            logger.error("No match found for message {}", message);
            return;
        }
        try {
            handler.handleChannelData(action, message);
        } catch (final BitfinexClientException e) {
            logger.error("Got exception while handling callback", e);
        }
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    public void onHeartbeatEvent(final Consumer<Long> heartbeatConsumer) {
        heartbeatHandler.onHeartbeatEvent(heartbeatConsumer);
    }

    public void onPositionsEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexPosition>> consumer) {
        positionHandler.onPositionsEvent(consumer);
    }

    public void onWalletsEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexWallet>> consumer) {
        walletHandler.onWalletsEvent(consumer);
    }

    public void onTradeEvent(final BiConsumer<BitfinexAccountSymbol, BitfinexMyExecutedTrade> tradeConsumer) {
        tradeHandler.onTradeEvent(tradeConsumer);
    }

    public void onSubmittedOrderEvent(final BiConsumer<BitfinexAccountSymbol, Collection<BitfinexSubmittedOrder>> consumer) {
        orderHandler.onSubmittedOrderEvent(consumer);
    }

    public void onOrderNotification(final BiConsumer<BitfinexAccountSymbol, BitfinexSubmittedOrder> consumer) {
        notificationHandler.onOrderNotification(consumer);
    }
}
