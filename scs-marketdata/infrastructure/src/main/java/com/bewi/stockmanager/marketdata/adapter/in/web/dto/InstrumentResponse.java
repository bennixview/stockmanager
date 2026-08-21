package com.bewi.stockmanager.marketdata.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Wkn;

@Schema(description = "An instrument this system can price")
public record InstrumentResponse(
        String symbol,
        String name,
        String isin,
        String wkn,
        String currency,
        String exchange,
        String type) {

    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
                instrument.symbol().value(),
                instrument.name(),
                instrument.isin().map(Isin::value).orElse(null),
                instrument.wkn().map(Wkn::value).orElse(null),
                instrument.currency().getCurrencyCode(),
                instrument.exchange().orElse(null),
                instrument.type().name());
    }
}
