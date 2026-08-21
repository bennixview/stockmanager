package com.bewi.stockmanager.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/** One OHLC bar of a price history. */
@ValueObject
public record Candle(Instant at, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {

    public Candle {
        Objects.requireNonNull(at, "at must not be null");
        Objects.requireNonNull(close, "close must not be null");
        open = Objects.requireNonNullElse(open, close);
        high = Objects.requireNonNullElse(high, close);
        low = Objects.requireNonNullElse(low, close);
    }
}
