package com.github.jnidzwetzki.bitfinex.v2;

public final class BitfinexClientFactory {

    private BitfinexClientFactory() {

    }

    /**
     * bitfinex client
     *
     * @param config - config
     * @return {@link SimpleBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient singleClient(BitfinexWebsocketConfiguration config) {
        return new SimpleBitfinexApiBroker(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor());
    }

    /**
     * bitfinex client with subscribed channel managed.
     * spreads amount of subscribed channels across multiple websocket physical connections.
     *
     * @param config                - config
     * @param channelsPerConnection - channels per client - 25 - 150 (limit by bitfinex exchange)
     * @return {@link PooledBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient pooledClient(BitfinexWebsocketConfiguration config, int channelsPerConnection) {
        return new PooledBitfinexApiBroker(config, new BitfinexApiCallbackRegistry(), new SequenceNumberAuditor(), channelsPerConnection);
    }

}
