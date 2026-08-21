package com.bewi.stockmanager.portfolio.domain;

import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * What this system needs to know about an instrument it holds.
 *
 * <p>A local copy of data the market data system owns. Copying it is the point: a position must
 * still render its name and currency when the other system is down, and the {@link Symbol} is all
 * that is needed to ask for a price once it is back.
 */
@ValueObject
public record InstrumentRef(Symbol symbol, String name, Isin isin, Wkn wkn, Currency currency) {

    public InstrumentRef {
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        name = (name == null || name.isBlank()) ? symbol.value() : name.trim();
    }

    public static InstrumentRef of(Symbol symbol, String name, Currency currency) {
        return new InstrumentRef(symbol, name, null, null, currency);
    }

    public Optional<Isin> isinValue() {
        return Optional.ofNullable(isin);
    }

    public Optional<Wkn> wknValue() {
        return Optional.ofNullable(wkn);
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(name, symbol);
    }
}
