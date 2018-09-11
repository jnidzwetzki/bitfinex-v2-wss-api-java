package com.github.jnidzwetzki.bitfinex.v2.command;

import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public interface SubscribeCommand extends BitfinexCommand {

    BitfinexStreamSymbol getSymbol();
}
