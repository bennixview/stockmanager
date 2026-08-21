package com.bewi.stockmanager.portfolio.domain;

import java.util.Objects;
import java.util.UUID;

import org.jmolecules.ddd.types.Identifier;

/** Identity of a {@link Position} inside its portfolio. */
public record PositionId(UUID value) implements Identifier {

    public PositionId {
        Objects.requireNonNull(value, "position id must not be null");
    }

    public static PositionId generate() {
        return new PositionId(UUID.randomUUID());
    }

    public static PositionId of(String value) {
        return new PositionId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
