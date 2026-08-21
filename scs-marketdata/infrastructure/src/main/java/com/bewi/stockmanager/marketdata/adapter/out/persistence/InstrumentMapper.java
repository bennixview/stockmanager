package com.bewi.stockmanager.marketdata.adapter.out.persistence;

import java.util.Currency;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentType;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Translates between the domain model and the rows this adapter stores. */
final class InstrumentMapper {

    private InstrumentMapper() {
    }

    static Instrument toDomain(InstrumentJpaEntity entity) {
        return Instrument.builder(Symbol.of(entity.getSymbol()))
                .name(entity.getName())
                .isin(entity.getIsin() == null ? null : Isin.of(entity.getIsin()))
                .wkn(entity.getWkn() == null ? null : Wkn.of(entity.getWkn()))
                .currency(Currency.getInstance(entity.getCurrency()))
                .exchange(entity.getExchange())
                .type(InstrumentType.valueOf(entity.getType()))
                .build();
    }

    static InstrumentJpaEntity toEntity(Instrument instrument) {
        return new InstrumentJpaEntity(
                instrument.symbol().value(),
                instrument.name(),
                instrument.isin().map(Isin::value).orElse(null),
                instrument.wkn().map(Wkn::value).orElse(null),
                instrument.currency().getCurrencyCode(),
                instrument.exchange().orElse(null),
                instrument.type().name());
    }
}
