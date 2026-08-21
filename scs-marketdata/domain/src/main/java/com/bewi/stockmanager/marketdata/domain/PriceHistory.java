package com.bewi.stockmanager.marketdata.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jmolecules.ddd.annotation.ValueObject;

/** An instrument's candles over a period, oldest first. */
@ValueObject
public record PriceHistory(Symbol symbol, HistoryRange range, List<Candle> candles) {

    public PriceHistory {
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(range, "range must not be null");
        candles = List.copyOf(candles).stream().sorted(Comparator.comparing(Candle::at)).toList();
    }

    public static PriceHistory empty(Symbol symbol, HistoryRange range) {
        return new PriceHistory(symbol, range, List.of());
    }

    public boolean isEmpty() {
        return candles.isEmpty();
    }

    public Optional<Candle> first() {
        return candles.isEmpty() ? Optional.empty() : Optional.of(candles.getFirst());
    }

    public Optional<Candle> last() {
        return candles.isEmpty() ? Optional.empty() : Optional.of(candles.getLast());
    }

    /** The performance over the whole range in percent, or empty when there is nothing to compare. */
    public Optional<BigDecimal> performancePercent() {
        return first().flatMap(from -> last().map(to -> {
            if (from.close().signum() == 0) {
                return BigDecimal.ZERO;
            }
            return to.close().subtract(from.close())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(from.close(), 4, java.math.RoundingMode.HALF_UP);
        }));
    }
}
