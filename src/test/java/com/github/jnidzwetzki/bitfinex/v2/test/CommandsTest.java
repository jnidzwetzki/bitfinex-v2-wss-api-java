package com.github.jnidzwetzki.bitfinex.v2.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBrokerConfig;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.command.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.CancelOrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.CancelOrderGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.PingCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SetConnectionFeaturesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexNewOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.exception.CommandException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.github.jnidzwetzki.bitfinex.v2.util.BitfinexStreamSymbolToChannelIdResolverAware;

public class CommandsTest {

	/**
	 * Call all commands and check for excepion
	 * @throws CommandException
	 */
	@Test
	public void testCommandsJSON() throws CommandException {

		final BitfinexNewOrder order
			= BitfinexOrderBuilder.create(
					BitfinexCurrencyPair.of("BCH","USD"), BitfinexOrderType.EXCHANGE_STOP, 2).build();

		final BitfinexCandlestickSymbol candleSymbol = BitfinexSymbols.candlesticks(BitfinexCurrencyPair.of("BCH","USD"), BitfinexCandleTimeFrame.HOUR_1);

		BitfinexOrderBookSymbol orderbookConfiguration = BitfinexSymbols.orderBook(BitfinexCurrencyPair.of("BCH", "USD"), BitfinexOrderBookSymbol.Precision.P0,
				BitfinexOrderBookSymbol.Frequency.F0, 50);

		BitfinexOrderBookSymbol rawOrderbookConfiguration = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("BAT", "BTC"));

		final List<BitfinexCommand> commands = Arrays.asList(
				new AuthCommand(AuthCommand.AUTH_NONCE_PRODUCER_TIMESTAMP),
				new CancelOrderCommand(123),
				new CancelOrderGroupCommand(1),
				new OrderCommand(order),
				new PingCommand(),
				new SubscribeCandlesCommand(candleSymbol),
				new SubscribeTickerCommand(BitfinexSymbols.ticker(BitfinexCurrencyPair.of("BCH","USD"))),
				new SubscribeTradesCommand(BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BAT","BTC"))),
				new SubscribeOrderbookCommand(orderbookConfiguration),
				new SubscribeOrderbookCommand(rawOrderbookConfiguration),
				new UnsubscribeChannelCommand(orderbookConfiguration),
				new SetConnectionFeaturesCommand(new HashSet<>()));

		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();

		for(final BitfinexCommand command : commands) {
			if (command instanceof BitfinexStreamSymbolToChannelIdResolverAware) {
				((BitfinexStreamSymbolToChannelIdResolverAware) command).setResolver(s -> 12);
			}
			final String commandValue = command.getCommand(bitfinexApiBroker);
			Assert.assertNotNull(commandValue);
			Assert.assertTrue(commandValue.length() > 10);
		}
	}

	/**
	 * Test the order command
	 * @throws CommandException
	 */
	@Test
	public void testOrderCommand() throws CommandException {
		final BitfinexNewOrder order
			= BitfinexOrderBuilder.create(BitfinexCurrencyPair.of("BCH","USD"), BitfinexOrderType.EXCHANGE_STOP, 2)
			.setHidden()
			.setPostOnly()
			.withPrice(12)
			.withPriceAuxLimit(23)
			.withPriceTrailing(23)
			.withGroupId(4)
			.build();

		final OrderCommand command = new OrderCommand(order);

		final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();

		final String commandValue = command.getCommand(bitfinexApiBroker);
		Assert.assertNotNull(commandValue);
		Assert.assertTrue(commandValue.length() > 10);
		Assert.assertTrue(commandValue.contains("\"2.0\""));
	}

	/**
	 *  Build the bitfinex connection
	 * @return
	 */
	private BitfinexApiBroker buildMockedBitfinexConnection() {
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final BitfinexApiBrokerConfig config = Mockito.mock(BitfinexApiBrokerConfig.class);

		Mockito.when(bitfinexApiBroker.getConfiguration()).thenReturn(config);
		Mockito.when(bitfinexApiBroker.getConfiguration().getApiKey()).thenReturn("abc");
		Mockito.when(bitfinexApiBroker.getConfiguration().getApiSecret()).thenReturn("123");
		return bitfinexApiBroker;
	}
}
