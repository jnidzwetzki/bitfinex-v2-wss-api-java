# Version 0.5.2 (TBA)

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

