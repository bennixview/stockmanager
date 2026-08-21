package com.bewi.stockmanager.portfolio.domain;

import java.time.LocalDate;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * One purchase lot of a position: shares bought on one day at one price.
 *
 * <p>Kept separate rather than folded into an average, because tax and performance both need to
 * know which shares were bought when.
 */
@ValueObject
public record Tranche(Quantity quantity, Money pricePerShare, Money fee, LocalDate tradeDate) {

    public Tranche {
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(pricePerShare, "pricePerShare must not be null");
        Objects.requireNonNull(tradeDate, "tradeDate must not be null");
        fee = Objects.requireNonNullElseGet(fee, () -> Money.zero(pricePerShare.currency()));
        if (quantity.isZero()) {
            throw new IllegalArgumentException("A tranche must hold shares");
        }
        if (!fee.currency().equals(pricePerShare.currency())) {
            throw new IllegalArgumentException("Fee and price must share a currency");
        }
    }

    public static Tranche of(Quantity quantity, Money pricePerShare, Money fee, LocalDate tradeDate) {
        return new Tranche(quantity, pricePerShare, fee, tradeDate);
    }

    /** What was paid for this lot, fee included. */
    public Money cost() {
        return quantity.at(pricePerShare).plus(fee);
    }

    /** The same lot with fewer shares; the fee shrinks proportionally so cost stays comparable. */
    Tranche reduceTo(Quantity remaining) {
        if (remaining.isGreaterThan(quantity)) {
            throw new IllegalArgumentException("Cannot grow a tranche by reducing it");
        }
        Money remainingFee = quantity.isZero()
                ? fee
                : Money.of(fee.amount().multiply(remaining.value())
                        .divide(quantity.value(), Money.SCALE, java.math.RoundingMode.HALF_UP), fee.currency());
        return new Tranche(remaining, pricePerShare, remainingFee, tradeDate);
    }

    Money feeFor(Quantity soldShares) {
        if (quantity.isZero()) {
            return Money.zero(fee.currency());
        }
        return Money.of(fee.amount().multiply(soldShares.value())
                .divide(quantity.value(), Money.SCALE, java.math.RoundingMode.HALF_UP), fee.currency());
    }
}
