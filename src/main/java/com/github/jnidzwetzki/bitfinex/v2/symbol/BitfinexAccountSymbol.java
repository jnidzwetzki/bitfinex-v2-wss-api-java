package com.github.jnidzwetzki.bitfinex.v2.symbol;

import java.util.Objects;
import java.util.Optional;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;

public class BitfinexAccountSymbol implements BitfinexStreamSymbol {

	 /**
     * The key permissions
     */
    private final BitfinexApiKeyPermissions permissions;

	/**
	 * The API key
	 */
    private final Optional<String> apiKey;
    
    BitfinexAccountSymbol(final BitfinexApiKeyPermissions permissions, final String apiKey) {
        this.permissions = permissions;
        this.apiKey = Optional.of(apiKey);
    }
    
    BitfinexAccountSymbol(final BitfinexApiKeyPermissions permissions) {
        this.permissions = permissions;
        this.apiKey = Optional.empty();
    }

    public Optional<String> getApiKey() {
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
