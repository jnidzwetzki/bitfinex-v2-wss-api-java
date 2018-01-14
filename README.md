# crypto-bot

[![Join the chat at https://gitter.im/bitfinex-v2-wss-api-java/Lobby](https://badges.gitter.im/bitfinex-v2-wss-api-java/Lobby.svg)](https://gitter.im/bitfinex-v2-wss-api-java/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<a href="https://travis-ci.org/jnidzwetzki/bitfinex-v2-wss-api-java">
  <img alt="Build Status" src="https://travis-ci.org/jnidzwetzki/bitfinex-v2-wss-api-java.svg?branch=master">
</a>
<a href="https://repo1.maven.org/maven2/com/github/jnidzwetzki/"><img alt="Maven Central Version" src="https://maven-badges.herokuapp.com/maven-central/com.github.jnidzwetzki/bitfinex-v2-wss-api/badge.svg" />
  </a><a href="https://codecov.io/gh/jnidzwetzki/bitfinex-v2-wss-api-java">
  <img src="https://codecov.io/gh/jnidzwetzki/bitfinex-v2-wss-api-java/branch/master/graph/badge.svg" />
</a><a href="https://gitter.im/bitfinex-v2-wss-api-java/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge">
  <img alt="Join the chat at https://gitter.im/bitfinex-v2-wss-api-java/Lobby" src="https://badges.gitter.im/Join%20Chat.svg">
  </a><a href="https://scan.coverity.com/projects/jnidzwetzki-bitfinex-v2-wss-api-java">
  <img alt="Coverity Scan Build Status"
       src="https://scan.coverity.com/projects/14740/badge.svg"/>
</a>

This project contains a client for the [Bitfinex WebSocket API (v2)](https://docs.bitfinex.com/v2/reference). At the moment, candles, ticks and (raw) orderbook streams are supported. In addition, orders, and wallets are also implemented.

In contrast to other implementations, this project uses the WSS streaming API of Bitfinex. Most other projects are poll the REST-API periodically, which leads to delays in data processing. In this implementation, you can register callback methods on ticks, candles or orders. The callbacks are executed, as soon as new data is received from Bitfinex (see the examples section for more details).

**Warning:** Trading carries significant financial risk; you could lose a lot of money. If you are planning to use this software to trade, you should perform many tests and simulations first. This software is provided 'as is' and released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). 


## Contact / Stay informed
* You need help or do you have questions? Join our chat at [gitter](https://gitter.im/bitfinex-v2-wss-api-java/Lobby)
* Visit our [bug tracking system](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/issues)
* Read the [source code](https://github.com/bitfinex-v2-wss-api-java/bboxdb)
* For contributing, see our [contributing guide](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/blob/master/CONTRIBUTING.md)

# Adding the library to your project

Add this to your pom.xml 

```xml
<dependency>
	<groupId>com.github.jnidzwetzki</groupId>
	<artifactId>bitfinex-v2-wss-api</artifactId>
	<version>0.5.2</version>
</dependency>
```

# Changelog
You will find the changelog of the project [here](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/blob/master/CHANGELOG.md).

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

## Working with connection capabilities
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

final BiConsumer<RawOrderbookConfiguration, OrderbookEntry> callback = (orderbookConfig, entry) -> {
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
			
final OrderbookManager orderbookManager = bitfinexApiBroker.getOrderbookManager();

final BiConsumer<RawOrderbookConfiguration, OrderbookEntry> callback = (orderbookConfig, entry) -> {
	System.out.format("Got entry (%s) for orderbook (%s)\n", entry, orderbookConfig);
};

orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
orderbookManager.subscribeOrderbook(orderbookConfiguration);

[...]

// To unsubscribe the raw orderbook stream
orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
orderbookManager.unsubscribeOrderbook(orderbookConfiguration);
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
