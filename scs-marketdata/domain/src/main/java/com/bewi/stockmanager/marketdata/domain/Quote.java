package com.bewi.stockmanager.marketdata.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The last known price of an instrument, plus what it takes to express the day's move.
 *
 * @param previousClose the reference price the change is measured against; may equal {@code price}
 *                      when a feed does not supply one
 */
@ValueObject
public record Quote(Symbol symbol, Money price, Money previousClose, MarketState marketState, Instant asOf) {

    public Quote {
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(asOf, "asOf must not be null");
        previousClose = Objects.requireNonNullElse(previousClose, price);
        marketState = Objects.requireNonNullElse(marketState, MarketState.UNKNOWN);
        if (!price.currency().equals(previousClose.currency())) {
            throw new IllegalArgumentException("price and previousClose must share a currency");
        }
    }

    public static Quote of(Symbol symbol, Money price, Money previousClose, Instant asOf) {
        return new Quote(symbol, price, previousClose, MarketState.UNKNOWN, asOf);
    }

    /** Absolute move since the previous close. */
    public Money change() {
        return price.minus(previousClose);
    }

    /** Relative move since the previous close, in percent. */
    public BigDecimal changePercent() {
        return previousClose.percentageChangeTo(price);
    }

    public boolean isUp() {
        return change().amount().signum() > 0;
    }

    /** Whether the quote is older than {@code maxAge} as of {@code now}. */
    public boolean isStale(Duration maxAge, Instant now) {
        return asOf.plus(maxAge).isBefore(now);
    }
}
