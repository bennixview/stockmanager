package com.bewi.stockmanager.portfolio.domain;

import java.util.Objects;
import java.util.UUID;

import org.jmolecules.ddd.types.Identifier;

/** Identity of a {@link Portfolio}. */
public record PortfolioId(UUID value) implements Identifier {

    public PortfolioId {
        Objects.requireNonNull(value, "portfolio id must not be null");
    }

    public static PortfolioId generate() {
        return new PortfolioId(UUID.randomUUID());
    }

    public static PortfolioId of(String value) {
        return new PortfolioId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
