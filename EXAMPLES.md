# Examples

If you have any questions about the meaning of the fields, see the Bitfinex [API documentation](https://docs.bitfinex.com/v2/docs/ws-general) for further information.

## Connecting and authorizing

```java
final String apiKey = "....";
final String apiSecret = "....";

// For public operations (subscribe ticker, candles)
BitfinexApiBroker bitfinexApiBroker = new BitfinexApiBroker(new BitfinexApiBrokerConfig());
bitfinexApiBroker.connect();

// For public and private operations (executing orders, read wallets)
BitfinexApiBrokerConfig config = new BitfinexApiBrokerConfig();
config.setApiCredentials(apiKey, apiSecret);

BitfinexApiBroker bitfinexApiBroker = new BitfinexApiBroker(config);
bitfinexApiBroker.connect();
```

## Connection capabilities
```java
final ConnectionCapabilities capabilities = bitfinexApiBroker.getCapabilities();

if(! capabilities.isHavingOrdersWriteCapability()) {
	System.err.println("This API key does not allow the placement of orders");
} else {
	// Trade
)
```

## Connection Features
```java
// Enabling package sequence auditing (ensuring all result packages are processed)
final ConnectionFeatureManager cfManager = bitfinexClient.getConnectionFeatureManager();

final SequenceNumberAuditor sequenceNumberAuditor = bitfinexClient.getSequenceNumberAuditor();
sequenceNumberAuditor.setErrorPolicy(SequenceNumberAuditor.ErrorPolicy.LOG_ONLY);

cfManager.enableConnectionFeature(BitfinexConnectionFeature.SEQ_ALL);
```

## Subscribe candles stream
```java

final BitfinexCandlestickSymbol symbol
	= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.of("BTC","USD"), Timeframe.MINUTE_1);

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

final BitfinexTickerSymbol symbol = new BitfinexTickerSymbol(BitfinexCurrencyPair.of("BTC","USD"));

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
final BitfinexExecutedTradeSymbol symbol = new BitfinexExecutedTradeSymbol(BitfinexCurrencyPair.of("BTC","USD"));

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

