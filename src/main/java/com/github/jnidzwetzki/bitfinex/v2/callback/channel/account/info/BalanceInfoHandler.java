/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.callback.channel.account.info;

import com.github.jnidzwetzki.bitfinex.v2.callback.channel.ChannelCallbackHandler;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexBalanceUpdate;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexAccountSymbol;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

public class BalanceInfoHandler implements ChannelCallbackHandler {

    private final int channelId;
    private final BitfinexAccountSymbol symbol;
    private BiConsumer<BitfinexAccountSymbol, BitfinexBalanceUpdate> balanceUpdateConsumer = (s, e) -> {};

    public BalanceInfoHandler(int channelId, BitfinexAccountSymbol symbol) {
        this.channelId = channelId;
        this.symbol = symbol;
    }

    @Override
    public void handleChannelData(String action, JSONArray message) throws BitfinexClientException {
        BigDecimal aum = message.getBigDecimal(0);
        BigDecimal aumNet = message.getBigDecimal(1);
        BitfinexBalanceUpdate bu = new BitfinexBalanceUpdate(aum, aumNet);
        balanceUpdateConsumer.accept(symbol, bu);
    }

    @Override
    public BitfinexStreamSymbol getSymbol() {
        return symbol;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    public void onBalanceUpdate(BiConsumer<BitfinexAccountSymbol, BitfinexBalanceUpdate> consumer) {
        balanceUpdateConsumer = consumer;
    }
}
