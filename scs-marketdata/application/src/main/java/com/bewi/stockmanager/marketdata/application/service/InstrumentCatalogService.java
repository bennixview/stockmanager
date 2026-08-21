package com.bewi.stockmanager.marketdata.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.application.port.in.InstrumentCatalog;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Use cases around the instrument catalogue. */
public class InstrumentCatalogService implements InstrumentCatalog {

    private static final int MAX_RESULTS = 100;

    private final InstrumentRepository instruments;

    public InstrumentCatalogService(InstrumentRepository instruments) {
        this.instruments = Objects.requireNonNull(instruments);
    }

    @Override
    public List<Instrument> search(String query, int limit) {
        return instruments.search(query, Math.clamp(limit, 1, MAX_RESULTS));
    }

    @Override
    public Optional<Instrument> bySymbol(Symbol symbol) {
        return instruments.findBySymbol(symbol);
    }

    @Override
    public Optional<Instrument> byIsin(Isin isin) {
        return instruments.findByIsin(isin);
    }

    @Override
    public Optional<Instrument> byWkn(Wkn wkn) {
        return instruments.findByWkn(wkn);
    }

    @Override
    public List<Instrument> all() {
        return instruments.findAll();
    }

    @Override
    public Instrument register(Instrument instrument) {
        return instruments.save(instrument);
    }
}
