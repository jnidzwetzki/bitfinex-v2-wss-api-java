package com.github.jnidzwetzki.bitfinex.v2;

import java.util.Collection;

import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.exception.APIException;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public interface BitfinexWebsocketClient {

    void connect() throws APIException;

    void close();

    void sendCommand(BitfinexCommand command);

    boolean reconnect();

    boolean unsubscribeAllChannels();

    boolean isAuthenticated();

    BitfinexApiKeyPermissions getApiKeyPermissions();

    Collection<BitfinexStreamSymbol> getSubscribedChannels();

    BitfinexWebsocketConfiguration getConfiguration();

    BitfinexApiCallbackListeners getCallbacks();

    QuoteManager getQuoteManager();

    OrderbookManager getOrderbookManager();

    RawOrderbookManager getRawOrderbookManager();

    PositionManager getPositionManager();

    OrderManager getOrderManager();

    TradeManager getTradeManager();

    WalletManager getWalletManager();

    ConnectionFeatureManager getConnectionFeatureManager();
}
