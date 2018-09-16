package com.github.jnidzwetzki.bitfinex.v2;

/**
 * bitfinex client factory
 */
public final class BitfinexClientFactory {

    private BitfinexClientFactory() {

    }

    /**
     * bitfinex client with default configuration - only public channels
     *
     * @return {@link SimpleBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newSimpleClient() {
        return new SimpleBitfinexApiBroker(new BitfinexWebsocketConfiguration(),
                new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
    }

    /**
     * bitfinex client
     *
     * @param config - config
     * @return {@link SimpleBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newSimpleClient(BitfinexWebsocketConfiguration config) {
        return new SimpleBitfinexApiBroker(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
    }

    /**
     * bitfinex client with subscribed channel managed.
     * spreads amount of subscribed channels across multiple websocket physical connections.
     *
     * @return {@link PooledBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newPooledClient() {
        return new PooledBitfinexApiBroker(new BitfinexWebsocketConfiguration(),new BitfinexApiCallbackRegistry(),
                new SequenceNumberAuditor(), 150);
    }

    /**
     * bitfinex client with subscribed channel managed.
     * spreads amount of subscribed channels across multiple websocket physical connections.
     *
     * @param config                - config
     * @param channelsPerConnection - channels per client - 25 - 250 (limit by bitfinex exchange)
     * @return {@link PooledBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newPooledClient(BitfinexWebsocketConfiguration config, int channelsPerConnection) {
        return new PooledBitfinexApiBroker(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor(), channelsPerConnection);
    }

}
