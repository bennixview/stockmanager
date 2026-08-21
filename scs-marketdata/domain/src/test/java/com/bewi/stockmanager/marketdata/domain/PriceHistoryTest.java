package com.bewi.stockmanager.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceHistoryTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void ordersCandlesOldestFirstWhateverOrderTheyArriveIn() {
        PriceHistory history = new PriceHistory(Symbol.of("AAPL"), HistoryRange.MONTH, List.of(
                candle(2, "120"), candle(0, "100"), candle(1, "110")));

        assertThat(history.candles()).extracting(Candle::close)
                .containsExactly(new BigDecimal("100"), new BigDecimal("110"), new BigDecimal("120"));
    }

    @Test
    void measuresPerformanceFromFirstToLastCandle() {
        PriceHistory history = new PriceHistory(Symbol.of("AAPL"), HistoryRange.MONTH,
                List.of(candle(0, "100"), candle(1, "125")));

        assertThat(history.performancePercent()).hasValueSatisfying(
                percent -> assertThat(percent).isEqualByComparingTo("25"));
    }

    @Test
    void hasNoPerformanceWithoutCandles() {
        assertThat(PriceHistory.empty(Symbol.of("AAPL"), HistoryRange.DAY).performancePercent()).isEmpty();
    }

    @Test
    void mapsRangeCodesUsedByTheApi() {
        assertThat(HistoryRange.fromCode("1d")).isEqualTo(HistoryRange.DAY);
        assertThat(HistoryRange.fromCode("3m")).isEqualTo(HistoryRange.QUARTER);
        assertThat(HistoryRange.fromCode(null)).isEqualTo(HistoryRange.MONTH);
    }

    private static Candle candle(int dayOffset, String close) {
        return new Candle(START.plusSeconds(dayOffset * 86_400L), null, null, null, new BigDecimal(close), 1_000);
    }
}
