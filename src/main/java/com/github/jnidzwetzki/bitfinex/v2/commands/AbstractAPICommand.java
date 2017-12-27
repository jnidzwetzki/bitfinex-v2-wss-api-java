package com.github.jnidzwetzki.bitfinex.v2.commands;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;

public abstract class AbstractAPICommand {

	public abstract String getCommand(final BitfinexApiBroker bitfinexApiBroker) throws CommandException;

}
