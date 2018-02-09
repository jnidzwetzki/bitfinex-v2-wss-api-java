# Version 0.5.6 (TBA)
* Improvement: Updated to ta4j 0.11 (Thanks brunocvcunha / closes #14)
* Improvement: Use codeclimate.com for code maintainability checks
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
* Improvement: Added further currencies (thanks to Flexz9)
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

