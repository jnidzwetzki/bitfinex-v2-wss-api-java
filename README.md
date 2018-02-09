# A Java implementation of the Bitfinex Websocket (v2) API

<a href="https://travis-ci.org/jnidzwetzki/bitfinex-v2-wss-api-java">
  <img alt="Build Status" src="https://travis-ci.org/jnidzwetzki/bitfinex-v2-wss-api-java.svg?branch=master">
</a>
<a href="https://repo1.maven.org/maven2/com/github/jnidzwetzki/"><img alt="Maven Central Version" src="https://maven-badges.herokuapp.com/maven-central/com.github.jnidzwetzki/bitfinex-v2-wss-api/badge.svg" />
  </a><a href="https://codecov.io/gh/jnidzwetzki/bitfinex-v2-wss-api-java">
  <img src="https://codecov.io/gh/jnidzwetzki/bitfinex-v2-wss-api-java/branch/master/graph/badge.svg" />
</a><a href="https://scan.coverity.com/projects/jnidzwetzki-bitfinex-v2-wss-api-java">
  <img alt="Coverity Scan Build Status"
       src="https://scan.coverity.com/projects/14740/badge.svg"/>
</a><a href="https://codeclimate.com/github/jnidzwetzki/bitfinex-v2-wss-api-java/maintainability"><img src="https://api.codeclimate.com/v1/badges/9bb1a95de6767a8c6820/maintainability" /></a>
<a href="https://gitter.im/bitfinex-v2-wss-api-java/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge">
  <img alt="Join the chat at https://gitter.im/bitfinex-v2-wss-api-java/Lobby" src="https://badges.gitter.im/Join%20Chat.svg">
  </a>

This project contains a client for the [Bitfinex WebSocket API (v2)](https://docs.bitfinex.com/v2/reference). At the moment, candles, ticks, executed trades and (raw) orderbook streams are supported. In addition, orders, and wallets are also implemented.

In contrast to other implementations, this project uses the WSS streaming API of Bitfinex. Most other projects are poll the REST-API periodically, which leads to delays in data processing. In this implementation, you can register callback methods on ticks, candles or orders. The callbacks are executed, as soon as new data is received from Bitfinex (see the examples section for more details).

**Warning:** Trading carries significant financial risk; you could lose a lot of money. If you are planning to use this software to trade, you should perform many tests and simulations first. This software is provided 'as is' and released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). 


## Contact / Stay informed
* You need help or do you have questions? Join our chat at [gitter](https://gitter.im/bitfinex-v2-wss-api-java/Lobby)
* For reporting issues, visit our [bug tracking system](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/issues)
* For contributing, see our [contributing guide](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/blob/master/CONTRIBUTING.md)

# Adding the library to your project
Add this to your pom.xml 

```xml
<dependency>
	<groupId>com.github.jnidzwetzki</groupId>
	<artifactId>bitfinex-v2-wss-api</artifactId>
	<version>0.5.5</version>
</dependency>
```

# Changelog
You will find the changelog of the project [here](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/blob/master/CHANGELOG.md).

# Examples
You will find some examples [here](https://github.com/jnidzwetzki/bitfinex-v2-wss-api-java/blob/master/EXAMPLES.md).

