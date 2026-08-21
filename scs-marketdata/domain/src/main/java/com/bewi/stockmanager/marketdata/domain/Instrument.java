package com.bewi.stockmanager.marketdata.domain;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A tradable instrument this system knows how to quote.
 *
 * <p>The ticker {@link Symbol} is the identity: it is what quote feeds and other systems address an
 * instrument by. ISIN and WKN are alternative keys used by German brokers and are optional, because
 * not every quotable thing (an index, a crypto pair) has them.
 */
@AggregateRoot
public final class Instrument {

    @Identity
    private final Symbol symbol;
    private final String name;
    private final Isin isin;
    private final Wkn wkn;
    private final Currency currency;
    private final String exchange;
    private final InstrumentType type;

    public Instrument(Symbol symbol, String name, Isin isin, Wkn wkn, Currency currency, String exchange,
            InstrumentType type) {
        this.symbol = Objects.requireNonNull(symbol, "symbol must not be null");
        this.name = requireText(name, "name");
        this.isin = isin;
        this.wkn = wkn;
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.exchange = exchange;
        this.type = Objects.requireNonNullElse(type, InstrumentType.OTHER);
    }

    public static Builder builder(Symbol symbol) {
        return new Builder(symbol);
    }

    public Symbol symbol() {
        return symbol;
    }

    public String name() {
        return name;
    }

    public Optional<Isin> isin() {
        return Optional.ofNullable(isin);
    }

    public Optional<Wkn> wkn() {
        return Optional.ofNullable(wkn);
    }

    public Currency currency() {
        return currency;
    }

    public Optional<String> exchange() {
        return Optional.ofNullable(exchange);
    }

    public InstrumentType type() {
        return type;
    }

    /** Whether this instrument matches a free-text search over symbol, name, ISIN and WKN. */
    public boolean matches(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return symbol.value().toLowerCase(Locale.ROOT).contains(needle)
                || name.toLowerCase(Locale.ROOT).contains(needle)
                || isin().map(i -> i.value().toLowerCase(Locale.ROOT).contains(needle)).orElse(false)
                || wkn().map(w -> w.value().toLowerCase(Locale.ROOT).contains(needle)).orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Instrument instrument && symbol.equals(instrument.symbol);
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(name, symbol);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    /** Builder, because an instrument has several optional alternative keys. */
    public static final class Builder {

        private final Symbol symbol;
        private String name;
        private Isin isin;
        private Wkn wkn;
        private Currency currency = Currency.getInstance("EUR");
        private String exchange;
        private InstrumentType type = InstrumentType.OTHER;

        private Builder(Symbol symbol) {
            this.symbol = symbol;
            this.name = symbol.value();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder isin(Isin isin) {
            this.isin = isin;
            return this;
        }

        public Builder wkn(Wkn wkn) {
            this.wkn = wkn;
            return this;
        }

        public Builder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder currency(String currencyCode) {
            return currency(Currency.getInstance(currencyCode));
        }

        public Builder exchange(String exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder type(InstrumentType type) {
            this.type = type;
            return this;
        }

        public Instrument build() {
            return new Instrument(symbol, name, isin, wkn, currency, exchange, type);
        }
    }
}
