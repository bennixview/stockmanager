package com.bewi.stockmanager.portfolio.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.bewi.stockmanager.portfolio.application.port.in.SearchInstruments;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/** Passes instrument lookups on to the system that owns them. */
public class InstrumentSearchService implements SearchInstruments {

    private static final int MAX_RESULTS = 20;

    private final MarketDataPort marketData;

    public InstrumentSearchService(MarketDataPort marketData) {
        this.marketData = Objects.requireNonNull(marketData);
    }

    @Override
    public List<InstrumentRef> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return marketData.search(query.trim(), MAX_RESULTS);
    }

    @Override
    public Optional<InstrumentRef> resolve(Symbol symbol) {
        return marketData.bySymbol(symbol);
    }
}
