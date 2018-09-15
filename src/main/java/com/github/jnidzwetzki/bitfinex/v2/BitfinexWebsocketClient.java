package com.github.jnidzwetzki.bitfinex.v2;

import java.util.Collection;

import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;
import com.github.jnidzwetzki.bitfinex.v2.manager.ConnectionFeatureManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.OrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.PositionManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.QuoteManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.RawOrderbookManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.TradeManager;
import com.github.jnidzwetzki.bitfinex.v2.manager.WalletManager;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

/**
 * Bitfinex Websocket Client exposing basic operations on server through commands
 */
public interface BitfinexWebsocketClient {

    /**
     * connects to bitfinex server
     */
    void connect();

    /**
     * closes connection with bitfinex server
     */
    void close();

    /**
     * sends command {@link BitfinexCommand} to server
     * @param command to execute on server
     */
    void sendCommand(BitfinexCommand command);

    /**
     * reconnects with server
     * @return true if reconnected
     */
    boolean reconnect();

    /**
     * convenient method of unsubscribing all channels
     * @return true if success
     */
    boolean unsubscribeAllChannels();

    /**
     * checks whether client is authenticated (permitted to execute user related events)
     * @return true if authenticated
     */
    boolean isAuthenticated();

    /**
     * retrieves api key permissions for this client
     * @return api key permissions
     */
    BitfinexApiKeyPermissions getApiKeyPermissions();

    /**
     * retrieves all subscribed channels in this client
     * @return collection of symbols
     */
    Collection<BitfinexStreamSymbol> getSubscribedChannels();

    /**
     * retrieves immutable view of configuration that this client was initialized with
     * @return configuration
     */
    BitfinexWebsocketConfiguration getConfiguration();

    /**
     * retrieves callbacks interface where user may register listeners
     * @return available callbacks
     */
    BitfinexApiCallbackListeners getCallbacks();

    /**
     * quote manager
     * @return quote manager
     */
    QuoteManager getQuoteManager();

    /**
     * convenient way to handle orderbook events
     * @return order book manager
     */
    OrderbookManager getOrderbookManager();

    /**
     * convenient way to handle raw orderbook events
     * @return raw orderbook manager
     */
    RawOrderbookManager getRawOrderbookManager();

    /**
     * convenient way to handle position events
     * @return position manager
     */
    PositionManager getPositionManager();

    /**
     * convenient way to handle (my) order events
     * @return order manager
     */
    OrderManager getOrderManager();

    /**
     * convenient way to handle executed trade events
     * @return trade manager
     */
    TradeManager getTradeManager();

    /**
     * convenient way to handle wallet events
     * @return wallet manager
     */
    WalletManager getWalletManager();

    /**
     * connection feature manager
     * @return connection feature manager
     */
    ConnectionFeatureManager getConnectionFeatureManager();
}
