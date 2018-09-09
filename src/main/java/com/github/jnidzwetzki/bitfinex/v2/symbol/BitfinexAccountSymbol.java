package com.github.jnidzwetzki.bitfinex.v2.symbol;

import java.util.Objects;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;

public class BitfinexAccountSymbol implements BitfinexStreamSymbol {

    private final String apiKey;
    private final BitfinexApiKeyPermissions permissions;

    public BitfinexAccountSymbol(String apiKey, BitfinexApiKeyPermissions permissions) {
        this.apiKey = apiKey;
        this.permissions = permissions;
    }

    public String getApiKey() {
        return apiKey;
    }

    public BitfinexApiKeyPermissions getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitfinexAccountSymbol that = (BitfinexAccountSymbol) o;
        return Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey);
    }

    @Override
    public String toString() {
        return "BitfinexAccountInfoSymbol [" +
                "apiKey='" + apiKey + '\'' +
                ']';
    }
}
