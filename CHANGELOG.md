# Version 0.6.3 (TBA)
* New Feature: Added the connection feature manager
* New Feature: Added sequence number audit (BitfinexConnectionFeature.SEQ_ALL)
* Improvement: Updated dependencies
* Improvement: Updated currency list (thanks ilyagalahov / closes #25)
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

