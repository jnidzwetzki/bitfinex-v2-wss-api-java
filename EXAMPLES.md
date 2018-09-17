# Examples

If you have any questions about the meaning of the fields, see the Bitfinex [API documentation](https://docs.bitfinex.com/v2/docs/ws-general) for further information.

Do not hesitate to report any issues/changes that we missed, as Bitfinex is contantly improving their API, and we don't keep track of them on every day basis.  

## Connecting and authorizing

```java
final String apiKey = "....";
final String apiSecret = "....";

// For public operations (subscribe ticker, candles)
BitfinexWebsocketClient client = BitfinexClientFactory.newSingleClient();
client.connect();

// For public and private operations (executing orders, read wallets)
BitfinexApiBrokerConfig config = new BitfinexApiBrokerConfig();
config.setApiCredentials(apiKey, apiSecret);

BitfinexWebsocketClient client = BitfinexClientFactory.newSingleClient(config);
client.connect();
```

## Provided API key permissions
```java
final BitfinexApiKeyPermissions permissions = client.getApiKeyPermissions();
if( !permissions.isOrderWritePermission() ) {
	System.err.println("This API key does not allow the placement of orders");
} else {
	// place order
)
```

## Available Currency Pairs
Client implementation depends internally on BitfinexCurrencyPair class which is abstraction of available currency pairs within exchange.
Since Bitfinex is introducing new currency pairs without any notice (and does it quite often), it's up to user to keep it, as library will raise an exception on unrecognized currency pair. 

To retrieve all available currency pairs user may fetch data from following URLs as described [here](https://docs.bitfinex.com/v1/reference#rest-public-symbols).
```
https://api.bitfinex.com/v1/symbols
https://api.bitfinex.com/v1/symbols_details
```

Since version 0.7.1, our library does not contain any hardcoded currency symbols anymore. The available currencies change very frequently and we are unable to keep the symbols up-to-date. You can register the needed currencies on your own or you can call the method `registerDefaults` which fetches all known currencies from Bitfinex and registers them.

```java
BitfinexCurrencyPair.registerDefaults();
```

However, as explained the user can register all currency pairs on their own by fetching one of mentioned REST services.

Here's a snippet of registering process 
```java
BitfinexCurrencyPair.register("BTC","USD", 0.001); // currency, profitCurrency, minimalOrderSize
```
Library will raise exception during registration if currency pair is already existing. To prevent that user should call:
```java
BitfinexCurrencyPair.unregisterAll()
```  

PS. _minimalOrderSize_ is not used any way by library - so user may pass 0.0d if not interested in that value. 
 

## Connection Features
```java
// Enabling package sequence auditing (ensuring all result packages are processed)
final ConnectionFeatureManager cfManager = bitfinexClient.getConnectionFeatureManager();

final SequenceNumberAuditor sequenceNumberAuditor = bitfinexClient.getSequenceNumberAuditor();
sequenceNumberAuditor.setErrorPolicy(SequenceNumberAuditor.ErrorPolicy.LOG_ONLY);

cfManager.enableConnectionFeature(BitfinexConnectionFeature.SEQ_ALL);
```
# Channel subscriptions
In following chapter, we shall provide some examples on how to subscribe to different channels and listen on their events.
API user needs to ensure client is firstly connected, as listeners do not perform channel subscriptions.

## Low-level channel subscription
Library exposes all events that are received. Here's an end-to-end snippet 

```
(1)     BitfinexCurrencyPair.registerDefaults();
(2a)    var config = new BitfinexApiBrokerConfig();
        config.setApiCredentials("user-api-key", "user-api-secret");
(2b)    var client = BitfinexClientFactory.newSingleClient(config);
(2c)    client.connect();
(3)     var symbol = BitfinexSymbols.*("curr1","curr2", [...]);
(4)     client.sendCommand(BitfinexCommands.subscribe*Channel(symbol));
(5)     Closeable callbackHandler = client.getCallbacks().on*Event((symbol, payload) -> {
            // handle event
        })
(6a)    callbackHandler.close();
(6b)    client.sendCommand(BitfinexCommands.unsubscribeChannel(symbol));

1 - registering bitfienx currency pairs preset in JVM 
2 - client setup 
3 - symbol creation
4 - subscribing to channel for websocket events
5 - registering listener for all events on given type (not per-symbol - for that functionality revert to managers)
6a - unregistering listener (events are still received and client still internally handles them)
6b - unsubscribing from channel (events no longer come)

6a and 6b are not depending on one another - user may wish to keep listeners registered to channels which are not operable (at given time)
```  

Library API exposes easy way of setting up subscriptions and handling events through Managers. Following examples will depend on them.

## Subscribe candles stream
```java

final BitfinexCandlestickSymbol symbol
	= BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BTC","USD"), Timeframe.MINUTE_1);

// The consumer will be called on all received candles for the symbol
final BiConsumer<BitfinexCandlestickSymbol, BitfinexCandle> callback = (sym, tick) -> {
	System.out.format("Got BitfinexTick (%s) for symbol (%s)\n", tick, sym);
};

final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
quoteManager.registerCandlestickCallback(symbol, callback);
quoteManager.subscribeCandles(symbol);

[...]

// To unsubscribe the candles stream
quoteManager.removeCandlestickCallback(symbol, callback);
tickerManager.unsubscribeCandles(symbol);
```

## Subscribe ticker stream
```java
// The consumer will be called on all received ticks for the symbol
final BiConsumer<BitfinexTickerSymbol, BitfinexTick> callback = (symbol, tick) -> {
	System.out.format("Got BitfinexTick (%s) for symbol (%s)\n", tick, symbol);
};

final BitfinexTickerSymbol symbol = BitfinexSymbols.ticker(BitfinexCurrencyPair.of("BTC","USD"));

final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
quoteManager.registerTickCallback(symbol, callback);
tickerManager.subscribeTicker(symbol);

[...]

// To unsubscribe the ticker stream
quoteManager.removeTickCallback(symbol, callback);
tickerManager.unsubscribeTicker(symbol);
```

## Subscribe orderbook stream
```java
final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
			BitfinexCurrencyPair.of("BTC","USD"), OrderBookPrecision.P0, OrderBookFrequency.F0, 25);

final OrderbookManager orderbookManager = bitfinexApiBroker.getOrderbookManager();

final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (orderbookConfig, entry) -> {
	System.out.format("Got entry (%s) for orderbook (%s)\n", entry, orderbookConfig);
};

orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
orderbookManager.subscribeOrderbook(orderbookConfiguration);

[...]

// To unsubscribe the orderbook stream
orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
orderbookManager.unsubscribeOrderbook(orderbookConfiguration);
```

## Subscribe raw orderbook stream
```java
final RawOrderbookConfiguration orderbookConfiguration = new RawOrderbookConfiguration(
			BitfinexCurrencyPair.of("BTC","USD"));

final RawOrderbookManager rawOrderbookManager = bitfinexClient.getRawOrderbookManager();

final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback = (orderbookConfig, entry) -> {
	System.out.format("Got entry (%s) for orderbook (%s)\n", entry, orderbookConfig);
};

rawOrderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
rawOrderbookManager.subscribeOrderbook(orderbookConfiguration);

[...]

// To unsubscribe the raw orderbook stream
rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
rawOrderbookManager.unsubscribeOrderbook(orderbookConfiguration);
```

## Executed trade callbacks (all trades on the exchange)
```java
final BitfinexExecutedTradeSymbol symbol = BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BTC","USD"));

final QuoteManager quoteManager = bitfinexClient.getQuoteManager();

final BiConsumer<BitfinexExecutedTradeSymbol, ExecutedTrade> callback = (symbol, trade) -> {
	System.out.format("Got executed trade (%s) for symbol (%s)\n", trade, symbol);
};

quoteManager.registerExecutedTradeCallback(symbol, callback);
quoteManager.subscribeExecutedTrades(symbol);

[...]

// To unsubscribe the executed trades stream
quoteManager.removeExecutedTradeCallback(symbol, callback);
quoteManager.unsubscribeExecutedTrades(symbol);
```

## Trade callbacks (generated by own orders)
```java
final TradeManager tradeManager = bitfinexApiBroker.getTradeManager();

tradeManager.registerCallback((trade) -> {
	System.out.format("Got trade callback (%s)\n", trade);
}
```

## Placing orders

The following order types are supported:

|   Ordertype   |            API Constant Marketplace          |          API Constant Margin         |
| ------------- | -------------------------------------------- | ------------------------------------ |
| Market        | ``BitfinexOrderType.EXCHANGE_MARKET``        | ``BitfinexOrderType.MARKET``         |
| Limit         | ``BitfinexOrderType.EXCHANGE_LIMIT``         | ``BitfinexOrderType.LIMIT``          |
| Stop          | ``BitfinexOrderType.EXCHANGE_STOP``          | ``BitfinexOrderType.STOP``           |
| Trailing stop | ``BitfinexOrderType.EXCHANGE_TRAILING_STOP`` | ``BitfinexOrderType.TRAILING_STOP``  |
| Fill or kill  | ``BitfinexOrderType.EXCHANGE_FOK``           | ``BitfinexOrderType.FOK``            |
| Stop Limit    | ``BitfinexOrderType.EXCHANGE_STOP_LIMIT``    | ``BitfinexOrderType.STOP_LIMIT``     |

_Please note:_ A negative amount will sell the currency pair, a positive amount will buy the currency pair.

```java
final BitfinexOrder order = BitfinexOrderBuilder
		.create(currency, BitfinexOrderType.MARKET, 0.002)
		.build();

bitfinexApiBroker.getOrderManager().placeOrder(order);
```

## Order group
```java
final CurrencyPair currencyPair = BitfinexCurrencyPair.of("BTC","USD");
final BitfinexTick lastValue = bitfinexApiBroker.getQuoteManager().getLastTick(currencyPair);

final int orderGroup = 4711;

// Long order when price rises 1%
final BitfinexOrder bitfinexOrder1 = BitfinexOrderBuilder
		.create(currencyPair, BitfinexOrderType.EXCHANGE_LIMIT, 0.002, lastValue.getClosePrice() / 100.0 * 101.0)
		.setPostOnly()
		.withGroupId(orderGroup)
		.build();

// Short order when price drops 1%
final BitfinexOrder bitfinexOrder2 = BitfinexOrderBuilder
		.create(currencyPair, BitfinexOrderType.EXCHANGE_LIMIT, -0.002, lastValue.getClosePrice() / 100.0 * 99.0)
		.setPostOnly()
		.withGroupId(orderGroup)
		.build();

// Cancel sell order when buy order failes
final Consumer<ExchangeOrder> ordercallback = (e) -> {

	if(e.getCid() == bitfinexOrder1.getCid()) {
		if(e.getState().equals(ExchangeOrder.STATE_CANCELED)
				|| e.getState().equals(ExchangeOrder.STATE_POSTONLY_CANCELED)) {
			bitfinexApiBroker.cancelOrderGroup(orderGroup);
		}
	}
};

final OrderManager orderManager = bitfinexApiBroker.getOrderManager();

orderManager.addOrderCallback(ordercallback);
orderManager.placeOrder(bitfinexOrder1);
orderManager.placeOrder(bitfinexOrder2);
```

## Request wallet balance
Some fields are not automatically calculated by the Bitfinex API, these values must be requested explicitly. This can be done by the `calculateWalletMarginBalancecalculateWalletMarginBalance` method.

```java
System.out.println(bitfinexApiBroker.getWalletManager().getWalletTable().get("margin", "USD"));

bitfinexApiBroker.getWalletManager().calculateWalletMarginBalance("USD");

// Wait some time until Bitfinex has send us a wallet update
Thread.sleep(TimeUnit.SECONDS.toMillis(5));

System.out.println(bitfinexApiBroker.getWalletManager().getWalletTable().get("margin", "USD"));
```
