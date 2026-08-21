package com.bewi.stockmanager.marketdata.application.port.in;

import java.util.List;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Driving port: look instruments up and add new ones to the catalogue. */
public interface InstrumentCatalog {

    List<Instrument> search(String query, int limit);

    Optional<Instrument> bySymbol(Symbol symbol);

    Optional<Instrument> byIsin(Isin isin);

    Optional<Instrument> byWkn(Wkn wkn);

    List<Instrument> all();

    Instrument register(Instrument instrument);
}
