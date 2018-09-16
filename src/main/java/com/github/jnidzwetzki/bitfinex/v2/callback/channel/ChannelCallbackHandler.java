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
package com.github.jnidzwetzki.bitfinex.v2.callback.channel;

import org.json.JSONArray;

import com.github.jnidzwetzki.bitfinex.v2.exception.BitfinexClientException;
import com.github.jnidzwetzki.bitfinex.v2.symbol.BitfinexStreamSymbol;

public interface ChannelCallbackHandler {

    /**
     * Handle data for the channel
     *
     * @param action        - channel action (hb, te/tu etc.)
     * @param message       - json message
     * @throws BitfinexClientException raised in case of exception
     */
    void handleChannelData(final String action, final JSONArray message) throws BitfinexClientException;

    /**
     * returns channel symbol
     */
    BitfinexStreamSymbol getSymbol();

    /**
     * returns channel id
     */
    int getChannelId();
}
