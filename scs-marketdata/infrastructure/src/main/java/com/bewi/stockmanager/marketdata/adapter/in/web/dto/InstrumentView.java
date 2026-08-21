package com.bewi.stockmanager.marketdata.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/**
 * What a page shows about an instrument, in types a template can print.
 *
 * <p>Keeping optionals and value objects out of the templates means the markup stays readable and
 * the domain model stays free to change shape.
 */
public record InstrumentView(String symbol, String name, String isin, String wkn, String currency, String exchange,
        String type) {

    private static final String NOTHING = "-";

    public static InstrumentView from(Instrument instrument) {
        return new InstrumentView(
                instrument.symbol().value(),
                instrument.name(),
                instrument.isin().map(Isin::value).orElse(NOTHING),
                instrument.wkn().map(Wkn::value).orElse(NOTHING),
                instrument.currency().getCurrencyCode(),
                instrument.exchange().orElse(NOTHING),
                instrument.type().name());
    }

    /** The chart on the detail page: closes as a JSON array, and the performance over the range. */
    public record HistoryView(String range, String series, BigDecimal performancePercent, boolean empty,
            List<RangeOption> ranges) {

        public static HistoryView from(PriceHistory history) {
            return new HistoryView(
                    history.range().code(),
                    history.candles().stream()
                            .map(candle -> candle.close().toPlainString())
                            .collect(java.util.stream.Collectors.joining(",", "[", "]")),
                    history.performancePercent().orElse(BigDecimal.ZERO),
                    history.isEmpty(),
                    java.util.Arrays.stream(HistoryRange.values())
                            .map(range -> new RangeOption(range.code(),
                                    range.code().toUpperCase(java.util.Locale.ROOT),
                                    range == history.range()))
                            .toList());
        }

        /** One button of the range switcher. */
        public record RangeOption(String code, String label, boolean active) {
        }
    }
}
