package com.bewi.stockmanager.marketdata.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The ticker symbol an instrument is traded under.
 *
 * <p>Broad on purpose: besides plain tickers like {@code AAPL} it has to carry the conventions the
 * quote feeds use - {@code SAP.DE} for an exchange, {@code ^GSPC} for an index, {@code BRK-B} for a
 * share class and {@code EURUSD=X} for a currency pair.
 */
@ValueObject
public record Symbol(String value) implements Comparable<Symbol> {

    private static final Pattern VALID = Pattern.compile("[A-Z0-9^][A-Z0-9._^=-]{0,19}");

    public Symbol {
        Objects.requireNonNull(value, "symbol must not be null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a valid ticker symbol: '" + value + "'");
        }
    }

    public static Symbol of(String value) {
        return new Symbol(value);
    }

    @Override
    public int compareTo(Symbol other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
