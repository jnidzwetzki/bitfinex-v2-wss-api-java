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
package com.github.jnidzwetzki.bitfinex.v2.command;

import java.util.Collection;
import java.util.regex.Pattern;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexCommandException;
import com.google.common.base.Preconditions;

public class OrderMultiCommand implements BitfinexOrderCommand {

    private final static Pattern REGEX = Pattern.compile("0,\\s*(.*),\\s*null", Pattern.DOTALL);
    private final BitfinexOrderCommand[] commands;

    public OrderMultiCommand(final Collection<BitfinexOrderCommand> commands) {
        Preconditions.checkArgument(!commands.isEmpty(), "Commands list cannot be empty!");
        Preconditions.checkArgument(commands.size() <= 15, "Commands list cannot be bigger than 15!");
        this.commands = commands.toArray(new BitfinexOrderCommand[0]);
    }

    @Override
    public String getCommand(BitfinexWebsocketClient client) throws BitfinexCommandException {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (BitfinexOrderCommand command : commands) {
            String json = REGEX.matcher(command.getCommand(client)).replaceAll("$1");
            sb.append(prefix).append(json);
            prefix = ",";
        }
        return "[0, \"ox_multi\", null, [" + sb.toString() + "]]";
    }
}
