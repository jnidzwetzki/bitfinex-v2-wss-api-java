package com.github.jnidzwetzki.bitfinex.v2.test;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexOrderBuilder;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketConfiguration;
import com.github.jnidzwetzki.bitfinex.v2.SimpleBitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.command.AuthCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.BitfinexCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelAllCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderCancelGroupCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderMultiCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderNewCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.OrderUpdateCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.PingCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SetConnectionFeaturesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeCandlesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeOrderbookCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTickerCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.SubscribeTradesCommand;
import com.github.jnidzwetzki.bitfinex.v2.command.UnsubscribeChannelCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexCandleTimeFrame;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderFlag;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexSubmittedOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.currency.BitfinexCurrencyPair;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexCandlestickSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexOrderBookSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexSymbols;
import com.github.jnidzwetzki.bitfinex.v2.util.BitfinexStreamSymbolToChannelIdResolverAware;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CommandsTest {

	@BeforeClass
	public static void registerDefaultCurrencyPairs() {
		if(BitfinexCurrencyPair.values().size() < 10) {
			BitfinexCurrencyPair.unregisterAll();
			BitfinexCurrencyPair.registerDefaults();
		}
	}

	/**
	 * Call all commands and check for exception
	 * @throws BitfinexCommandException
	 */
	@Test
	public void testCommandsJSON() throws BitfinexCommandException {

		final BitfinexCurrencyPair currencyPair = BitfinexCurrencyPair.of("BTC","USD");
		
		final BitfinexOrder order = BitfinexOrderBuilder.create(
					currencyPair, BitfinexOrderType.EXCHANGE_STOP, 2).build();
		
		final BitfinexSubmittedOrder submittedOrder = new BitfinexSubmittedOrder();
		submittedOrder.setCurrencyPair(currencyPair);
		submittedOrder.setOrderId(1234L);

		final BitfinexCandlestickSymbol candleSymbol = BitfinexSymbols.candlesticks(currencyPair, BitfinexCandleTimeFrame.HOUR_1);

		BitfinexOrderBookSymbol orderbookConfiguration = BitfinexSymbols.orderBook(currencyPair, BitfinexOrderBookSymbol.Precision.P0,
				BitfinexOrderBookSymbol.Frequency.F0, 50);

		BitfinexOrderBookSymbol rawOrderbookConfiguration = BitfinexSymbols.rawOrderBook(BitfinexCurrencyPair.of("BAT", "BTC"));

		final List<BitfinexCommand> commands = Arrays.asList(
				new AuthCommand(AuthCommand.AUTH_NONCE_PRODUCER_TIMESTAMP),
				new OrderCancelCommand(123),
				new OrderCancelGroupCommand(1),
				new OrderNewCommand(order),
				new OrderUpdateCommand(submittedOrder),
				new PingCommand(),
				new SubscribeCandlesCommand(candleSymbol),
				new SubscribeTickerCommand(BitfinexSymbols.ticker(currencyPair)),
				new SubscribeTradesCommand(BitfinexSymbols.executedTrades(BitfinexCurrencyPair.of("BAT","BTC"))),
				new SubscribeOrderbookCommand(orderbookConfiguration),
				new SubscribeOrderbookCommand(rawOrderbookConfiguration),
				new UnsubscribeChannelCommand(orderbookConfiguration),
				new SetConnectionFeaturesCommand(new HashSet<>()));

		final BitfinexWebsocketClient bitfinexApiBroker = buildMockedBitfinexConnection();

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
	 * @throws BitfinexCommandException
	 */
	@Test
	public void testOrderCommand() throws BitfinexCommandException {
		final BitfinexOrder order
			= BitfinexOrderBuilder.create(BitfinexCurrencyPair.of("BTC","USD"), BitfinexOrderType.EXCHANGE_STOP, 2)
			.withOrderFlag(BitfinexOrderFlag.HIDDEN)
			.withPrice(12)
			.withPriceAuxLimit(23)
			.withPriceTrailing(23)
			.withGroupId(4)
			.build();

		final OrderNewCommand command = new OrderNewCommand(order);

		final BitfinexWebsocketClient bitfinexApiBroker = buildMockedBitfinexConnection();

		final String commandValue = command.getCommand(bitfinexApiBroker);
		Assert.assertNotNull(commandValue);
		Assert.assertTrue(commandValue.length() > 10);
		Assert.assertTrue(commandValue.contains("\"2.0\""));
	}

	@Test
	public void testOrderMultiOperationCommand_ok() throws BitfinexCommandException {
		// given
		final BitfinexOrder order
				= BitfinexOrderBuilder.create(BitfinexCurrencyPair.of("BTC","USD"), BitfinexOrderType.EXCHANGE_STOP, 2)
				.withOrderFlag(BitfinexOrderFlag.HIDDEN)
				.withPrice(12)
				.withPriceAuxLimit(23)
				.withPriceTrailing(23)
				.withGroupId(4)
				.build();
		order.setClientId(100L);

		OrderCancelCommand c1 = new OrderCancelCommand(1);
		OrderNewCommand c2 = new OrderNewCommand(order);
		OrderCancelGroupCommand c3 = new OrderCancelGroupCommand(3);
		OrderCancelAllCommand c4 = new OrderCancelAllCommand();

        // when
		OrderMultiCommand multiCommand = new OrderMultiCommand(Lists.newArrayList(c1, c2, c3, c4));
		String command = multiCommand.getCommand(null);

		// then
		Assert.assertEquals("[0, \"ox_multi\", null, [[\"oc\", {\"id\":1}],[\"on\", {\"symbol\":\"tBTCUSD\",\"amount\":\"2.0\",\"gid\":4,\"price\":\"12.0\",\"flags\":64,\"price_aux_limit\":\"23.0\",\"type\":\"EXCHANGE STOP\",\"price_trailing\":\"23.0\",\"cid\":100}],[\"oc_multi\", {\"gid\":[3]}],[\"oc_multi\", {\"all\": 1}]]]", command);
	}

	private BitfinexWebsocketClient buildMockedBitfinexConnection() {
		final BitfinexWebsocketClient bitfinexApiBroker = Mockito.mock(SimpleBitfinexApiBroker.class);
		final BitfinexWebsocketConfiguration config = Mockito.mock(BitfinexWebsocketConfiguration.class);

		Mockito.when(bitfinexApiBroker.getConfiguration()).thenReturn(config);
		Mockito.when(bitfinexApiBroker.getConfiguration().getApiKey()).thenReturn("abc");
		Mockito.when(bitfinexApiBroker.getConfiguration().getApiSecret()).thenReturn("123");
		return bitfinexApiBroker;
	}
}
