package com.bewi.stockmanager.marketdata.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.bewi.stockmanager.marketdata.domain.PriceHistory;

@Schema(description = "Candles of an instrument over a period")
public record PriceHistoryResponse(String symbol, String range, BigDecimal performancePercent, List<CandleResponse> candles) {

    public static PriceHistoryResponse from(PriceHistory history) {
        return new PriceHistoryResponse(
                history.symbol().value(),
                history.range().name(),
                history.performancePercent().orElse(null),
                history.candles().stream()
                        .map(candle -> new CandleResponse(candle.at(), candle.open(), candle.high(), candle.low(),
                                candle.close(), candle.volume()))
                        .toList());
    }

    public record CandleResponse(Instant at, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
            long volume) {
    }
}
