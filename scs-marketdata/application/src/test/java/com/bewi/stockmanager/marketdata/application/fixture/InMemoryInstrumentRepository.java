package com.bewi.stockmanager.marketdata.application.fixture;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** In-memory stand-in for the repository port, so use cases can be tested without a database. */
public class InMemoryInstrumentRepository implements InstrumentRepository {

    private final Map<Symbol, Instrument> instruments = new LinkedHashMap<>();

    @Override
    public Instrument save(Instrument instrument) {
        instruments.put(instrument.symbol(), instrument);
        return instrument;
    }

    @Override
    public Optional<Instrument> findBySymbol(Symbol symbol) {
        return Optional.ofNullable(instruments.get(symbol));
    }

    @Override
    public Optional<Instrument> findByIsin(Isin isin) {
        return instruments.values().stream().filter(i -> i.isin().filter(isin::equals).isPresent()).findFirst();
    }

    @Override
    public Optional<Instrument> findByWkn(Wkn wkn) {
        return instruments.values().stream().filter(i -> i.wkn().filter(wkn::equals).isPresent()).findFirst();
    }

    @Override
    public List<Instrument> search(String query, int limit) {
        return instruments.values().stream().filter(i -> i.matches(query)).limit(limit).toList();
    }

    @Override
    public List<Instrument> findAll() {
        return List.copyOf(instruments.values());
    }

    @Override
    public void delete(Symbol symbol) {
        instruments.remove(symbol);
    }
}
