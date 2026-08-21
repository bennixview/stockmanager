package com.bewi.stockmanager.portfolio.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/** A number of shares. Fractional, because savings plans and most brokers deal in fractions. */
@ValueObject
public record Quantity(BigDecimal value) implements Comparable<Quantity> {

    public static final int SCALE = 6;
    public static final Quantity ZERO = new Quantity(BigDecimal.ZERO);

    public Quantity {
        Objects.requireNonNull(value, "quantity must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Quantity must not be negative: " + value);
        }
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Quantity of(BigDecimal value) {
        return new Quantity(value);
    }

    public static Quantity of(String value) {
        return new Quantity(new BigDecimal(value));
    }

    public static Quantity of(long value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    public Quantity plus(Quantity other) {
        return new Quantity(value.add(other.value));
    }

    public Quantity minus(Quantity other) {
        return new Quantity(value.subtract(other.value));
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isGreaterThan(Quantity other) {
        return compareTo(other) > 0;
    }

    /** The value of this many shares at the given price per share. */
    public Money at(Money pricePerShare) {
        return pricePerShare.times(value);
    }

    @Override
    public int compareTo(Quantity other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString();
    }
}
