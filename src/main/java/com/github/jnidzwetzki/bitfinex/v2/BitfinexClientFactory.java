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
        return newSimpleClient(new BitfinexWebsocketConfiguration());
    }

    /**
     * bitfinex client
     *
     * @param config - config
     * @return {@link SimpleBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newSimpleClient(final BitfinexWebsocketConfiguration config) {
        final BitfinexApiCallbackRegistry callbackRegistry = new BitfinexApiCallbackRegistry();
		final SequenceNumberAuditor sequenceNumberAuditor = new SequenceNumberAuditor();
		
		sequenceNumberAuditor.setErrorPolicy(config.getErrorPolicy());
		
		return new SimpleBitfinexApiBroker(config, callbackRegistry, sequenceNumberAuditor, false);
    }

    /**
     * bitfinex client with subscribed channel managed.
     * spreads amount of subscribed channels across multiple websocket physical connections.
     *
     * @return {@link PooledBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newPooledClient() {
        return newPooledClient(new BitfinexWebsocketConfiguration(), 30);
    }

    /**
     * bitfinex client with subscribed channel managed.
     * spreads amount of subscribed channels across multiple websocket physical connections.
     *
     * @param config                - config
     * @param channelsPerWebsocketConnection - channels per websocket connection - 5 - 30 (limit by bitfinex exchange)
     * @return {@link PooledBitfinexApiBroker} client
     */
    public static BitfinexWebsocketClient newPooledClient(final BitfinexWebsocketConfiguration config, 
    		final int channelsPerWebsocketConnection) {
    	
        if (channelsPerWebsocketConnection < 5 || channelsPerWebsocketConnection > 30) {
            throw new IllegalArgumentException("'channelsPerWebsocketConnection' must be in range [5, 30)");
        }
    	
        final BitfinexApiCallbackRegistry callbacks = new BitfinexApiCallbackRegistry();
		final SequenceNumberAuditor sequenceNumberAuditor = new SequenceNumberAuditor();
		
		sequenceNumberAuditor.setErrorPolicy(config.getErrorPolicy());

		return new PooledBitfinexApiBroker(config, callbacks, sequenceNumberAuditor, channelsPerWebsocketConnection);
    }

}
