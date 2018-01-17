# Examples

## Connecting and authorizing

```java 
final String apiKey = "....";
final String apiSecret = "....";

// For public operations (subscribe ticker, bars)
BitfinexApiBroker bitfinexApiBroker = BitfinexApiBroker();
bitfinexApiBroker.connect();

// For public and private operations (executing orders, read wallets)
BitfinexApiBroker bitfinexApiBroker = BitfinexApiBroker(apiKey, apiSecret);
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

## Subscribe candles stream
```java

final BitfinexCandlestickSymbol symbol 
	= new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTE_1);
	
// The consumer will be called on all received candles for the symbol
final BiConsumer<BitfinexCandlestickSymbol, Tick> callback = (symbol, tick) -> {
	System.out.format("Got tick (%s) for symbol (%s)\n", tick, symbol);
};

final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
quoteManager.registerCandlestickCallback(symbol, callback);
tickerManager.subscribeCandles(symbol);

[...]

// To unsubscribe the candles stream
quoteManager.removeCandlestickCallback(symbol, callback);
tickerManager.unsubscribeCandles(symbol);
```

## Subscribe ticker stream
```java
// The consumer will be called on all received ticks for the symbol
final BiConsumer<BitfinexCurrencyPair, Tick> callback = (symbol, tick) -> {
	System.out.format("Got tick (%s) for symbol (%s)\n", tick, symbol);
};

final QuoteManager quoteManager = bitfinexApiBroker.getQuoteManager();
quoteManager.registerTickCallback(BitfinexCurrencyPair.BTC_USD, callback);
tickerManager.subscribeTicker(BitfinexCurrencyPair.BTC_USD);

[...]

// To unsubscribe the ticker stream
quoteManager.removeTickCallback(BitfinexCurrencyPair.BTC_USD, callback);
tickerManager.unsubscribeTicker(BitfinexCurrencyPair.BTC_USD);
```

## Subscribe orderbook stream
```java
final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
			BitfinexCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);
			
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
			BitfinexCurrencyPair.BTC_USD);

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

## Trade callbacks
```java
final TradeManager tradeManager = bitfinexApiBroker.getTradeManager();		
		
tradeManager.registerCallback((trade) -> {
	System.out.format("Got trade callback (%s)\n", trade);
}
```

## Market order

```java
final BitfinexOrder order = BitfinexOrderBuilder
		.create(currency, BitfinexOrderType.MARKET, 0.002)
		.build();
		
bitfinexApiBroker.getOrderManager().placeOrder(order);
```

## Order group

```java
final CurrencyPair currencyPair = CurrencyPair.BTC_USD;
final Tick lastValue = bitfinexApiBroker.getLastTick(currencyPair);

final int orderGroup = 4711;

final BitfinexOrder bitfinexOrder1 = BitfinexOrderBuilder
		.create(currencyPair, BitfinexOrderType.EXCHANGE_LIMIT, 0.002, lastValue.getClosePrice().toDouble() / 100.0 * 100.1)
		.setPostOnly()
		.withGroupId(orderGroup)
		.build();

final BitfinexOrder bitfinexOrder2 = BitfinexOrderBuilder
		.create(currencyPair, BitfinexOrderType.EXCHANGE_LIMIT, -0.002, lastValue.getClosePrice().toDouble() / 100.0 * 101)
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