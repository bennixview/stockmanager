package com.bewi.stockmanager.marketdata.adapter.in.web.dto;

import java.util.Currency;

import jakarta.validation.constraints.NotBlank;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentType;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Payload for adding an instrument to the catalogue. */
public record RegisterInstrumentRequest(
        @NotBlank String symbol,
        @NotBlank String name,
        String isin,
        String wkn,
        String currency,
        String exchange,
        String type) {

    public Instrument toDomain() {
        return Instrument.builder(Symbol.of(symbol))
                .name(name)
                .isin(isin == null || isin.isBlank() ? null : Isin.of(isin))
                .wkn(wkn == null || wkn.isBlank() ? null : Wkn.of(wkn))
                .currency(Currency.getInstance(currency == null || currency.isBlank() ? "EUR" : currency))
                .exchange(exchange)
                .type(type == null || type.isBlank() ? InstrumentType.OTHER : InstrumentType.valueOf(type))
                .build();
    }
}
