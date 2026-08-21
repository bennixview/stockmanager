package com.bewi.stockmanager.marketdata.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

import com.bewi.stockmanager.marketdata.domain.Quote;

/** The wire format of a quote. Owned by the adapter, so the domain stays free to change. */
@Schema(description = "Last known price of an instrument")
public record QuoteResponse(
        String symbol,
        BigDecimal price,
        String currency,
        BigDecimal previousClose,
        BigDecimal change,
        BigDecimal changePercent,
        String marketState,
        Instant asOf) {

    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.symbol().value(),
                quote.price().rounded(),
                quote.price().currency().getCurrencyCode(),
                quote.previousClose().rounded(),
                quote.change().rounded(),
                quote.changePercent(),
                quote.marketState().name(),
                quote.asOf());
    }
}
