# Version 0.7.1 (TBA)
* New Feature: The available currencies are now fetched directly from Bitfinex
* New Feature: Added WSv2 order flag support (see https://docs.bitfinex.com/v2/docs/changelog)
* Bugfix: Missing account-info-handler ("0"-channel) for "hb" event only when user not authenticated

# Version 0.7.0 (17.09.2018)
* New Feature: PooledBitfinexApiBroker - as per requirement described in https://www.bitfinex.com/posts/267
* New Feature: Added future operation objects
* Improvement: Updated dependencies
* Improvement: The architecture of the lib was refactored (thanks to mironbalcerzak / PRs: #57 - #74)
* Improvement: Factory classes (clients/symbols/commands) introduced
* Improvement: Common suffix for entities
* Improvement: Event listener API introduced
* Improvement: The APIException renamed
* Improvement: The APIException has changed from type Exception to RuntimeException
* Improvement: Removed unused JPA annotations
* Bugfix: Fixed race condition in BiConsumerCallback

# Version 0.6.9 (19.08.2018)
* New Feature: Made auth nonce producer configurable (thanks to mironbalcerzak / closes #43) 
* Improvement: Added dynamic currency creation (thanks to mironbalcerzak / closes #41 and #42) 

# Version 0.6.8 (14.08.2018)
* Improvement: Clean up channel freshness handling
* Improvement: Updated dependencies

# Version 0.6.7 (31.07.2018)
* New Feature: Add support for Bitfinex native ticks
* Bugfix: It was not possible to request the monthly candles

# Version 0.6.6 (10.05.2018)
* New Feature: Added the 'dead man switch' connection feature
* Improvement: Updated currency list (thanks ilyagalahov / closes #31)

# Version 0.6.5 (27.04.2018)
* Bugfix: Unsubscibe channels before symbol map restore is performed in re-connect
* Bugfix: Handle 'partially filled' order notifications correctly (closes #30)

# Version 0.6.4 (19.04.2018)
* Improvement: Replaced ArrayList by thread-safe variant in WebsocketClient to speed-up message processing
* Improvement: Handle error callbacks
* Bugfix: Increased reconnect timeout
* Bugfix: Handle error callbacks without currency symbol properly (closes #28)
* Bugfix: Fixed some BigDecimal / JSON related serialization issues (closes #29)

# Version 0.6.3 (14.04.2018)
* New Feature: Added the connection feature manager
* New Feature: Added sequence number audit (BitfinexConnectionFeature.SEQ_ALL)
* Improvement: Updated dependencies
* Improvement: Updated currency list (thanks ilyagalahov / closes #25)
* Improvement: Unified the value for uninitialized BigDecimal values to null (sometimes -1 was used)
* Improvement: Replaced BitfinexTick.INVALID_VOLUME invalid marker value with Java 8 Optional value
* Bugfix: Added missing BigDecimal order constructor (closes #23)
* Bugfix: Reverted FundingType to int and made field optional (closes #26)

# Version 0.6.2 (06.04.2018)
* Improvement: Switched from double data type to BigDecimal to increase precision (thanks hansblafoo / closes #20 / #22)
* Improvement: Clean up code

# Version 0.6.1 (23.03.2018)
* New Feature: Moved the wallets into a WalletManager
* New Feature: Support the 'calc' request, to calculate some wallet related data (closes #19)
* Improvement: Switched to tyrus-standalone-client-jdk (uses Java SE 7 Asynchronous IO) for increased performance
* Bugfix: Removed invalid currency symbol (closes #18)

# Version 0.6.0 (17.03.2018)
* Improvement: Removed ta4j dependency
* Improvement: Added new currency pairs (thanks ilyagalahov / closes #17)

# Version 0.5.7 (28.02.2018)
* Improvement: Floating point number accuracy increased (closes #16)

# Version 0.5.6 (10.02.2018)
* Improvement: Updated to ta4j 0.11 (thanks brunocvcunha / closes #14)
* Improvement: Use codeclimate.com for code maintainability checks
* Improvement: Improved code maintainability as suggested by codeclimate.com
* Improvement: The minimum order size of currencies can be changed at runtime
* Improvement: Updated dependencies
* Bugfix: Fixed executed trades parser (closes #12)
* Bugfix: "tu" messages are not forwarded on executed trades (closes #13)

# Version 0.5.5 (08.02.2018)
* New Feature: Provide a callback for all executed trades (closes #11)
* Improvement: Updated dependencies
* Bugfix: Ticker now returns a BitfinexTickerSymbol

# Version 0.5.4 (20.01.2018)
* New Feature: Introduced trade callbacks
* Improvement: Updated currency pair trade sizes

# Version 0.5.3 (16.01.2018)
* New Feature: Introduced the RawOrderBook Manager
* Improvement: Added further currencies (thanks Flexz9)
* Improvement: A APIException is thrown immediately after an authentication failure occurred
* Improvement: Added further timeframes
* Bugfix: Websocket messages up to 1 MB will be accepted

# Version 0.5.2 (11.01.2018)
* New Feature: Support raw orderbooks
* Improvement: Renamed trading orderbook to orderbook
* Bugfix: Fixed typo in funding capability (closes #1)

# Version 0.5.1 (06.01.2018)
* New Feature: Added connection capabilities
* Improvement: Added more unit tests
* Improvement: Moved order functions from connection to order manager
* Improvement: Added further currencies
* Bugfix: Fixed the parsing of orders without a cid

# Version 0.5.0 (04.01.2018)
* New feature: Support trade orderbooks
* Improvement: Added unit tests
* Improvement: Added subscribe / unsubscribe methods for candles and ticker to the ticker manager
* Improvement: Multiple candle streams for one symbol can now be subscribed simultaneously
* Improvement: Use 'Coverity Scan - Static Analysis' to find bugs in the code
* Bugfix: The ticks now a volume of 0
* Bugfix: Remove ticker / candle streams from ticker map on unsubscribe
* Bugfix: Fixed the re-subscription of candles on reconnect
* Bugfix: Positions are now available in the position manager

# Version 0.0.1 (27.12.2017)
* First version of this project
* Repository split form https://github.com/jnidzwetzki/crypto-bot

