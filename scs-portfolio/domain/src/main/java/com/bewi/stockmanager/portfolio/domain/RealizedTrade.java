package com.bewi.stockmanager.portfolio.domain;

import java.time.LocalDate;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/** A closed round trip: shares bought once and sold later, with the profit that came out of it. */
@ValueObject
public record RealizedTrade(
        Quantity quantity,
        Money buyPricePerShare,
        Money sellPricePerShare,
        Money fees,
        LocalDate buyDate,
        LocalDate sellDate) {

    public RealizedTrade {
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(buyPricePerShare, "buyPricePerShare must not be null");
        Objects.requireNonNull(sellPricePerShare, "sellPricePerShare must not be null");
        Objects.requireNonNull(buyDate, "buyDate must not be null");
        Objects.requireNonNull(sellDate, "sellDate must not be null");
        fees = Objects.requireNonNullElseGet(fees, () -> Money.zero(buyPricePerShare.currency()));
    }

    /** Profit or loss after fees. */
    public Money gain() {
        return quantity.at(sellPricePerShare).minus(quantity.at(buyPricePerShare)).minus(fees);
    }

    public Money proceeds() {
        return quantity.at(sellPricePerShare).minus(fees);
    }
}
