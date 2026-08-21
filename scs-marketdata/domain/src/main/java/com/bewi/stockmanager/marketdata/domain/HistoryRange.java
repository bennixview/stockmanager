package com.bewi.stockmanager.marketdata.domain;

import java.time.Duration;
import java.time.Period;
import java.util.Locale;

/** The periods of price history this system offers. */
public enum HistoryRange {

    DAY("1d", Period.ofDays(1), Duration.ofMinutes(5)),
    WEEK("1w", Period.ofDays(7), Duration.ofHours(1)),
    MONTH("1m", Period.ofMonths(1), Duration.ofDays(1)),
    QUARTER("3m", Period.ofMonths(3), Duration.ofDays(1)),
    YEAR("1y", Period.ofYears(1), Duration.ofDays(1)),
    FIVE_YEARS("5y", Period.ofYears(5), Duration.ofDays(7));

    private final String code;
    private final Period period;
    private final Duration interval;

    HistoryRange(String code, Period period, Duration interval) {
        this.code = code;
        this.period = period;
        this.interval = interval;
    }

    /** The short form used in URLs and shown on the range buttons, e.g. {@code 3m}. */
    public String code() {
        return code;
    }

    public Period period() {
        return period;
    }

    /** The spacing between candles that suits this range. */
    public Duration interval() {
        return interval;
    }

    /**
     * The range as a duration.
     *
     * <p>Months and years are not units an {@link java.time.Instant} understands, so anything doing
     * arithmetic on timestamps needs this approximation (a month counts as 30 days) rather than
     * {@link #period()}.
     */
    public Duration length() {
        return Duration.ofDays(period.toTotalMonths() * 30L + period.getDays());
    }

    public static HistoryRange fromCode(String code) {
        if (code == null || code.isBlank()) {
            return MONTH;
        }
        return switch (code.trim().toLowerCase(Locale.ROOT)) {
            case "1d", "day" -> DAY;
            case "1w", "week" -> WEEK;
            case "1m", "month" -> MONTH;
            case "3m", "quarter" -> QUARTER;
            case "1y", "year" -> YEAR;
            case "5y" -> FIVE_YEARS;
            default -> throw new IllegalArgumentException("Unknown history range: '" + code + "'");
        };
    }
}
