package com.bewi.stockmanager.marketdata.domain;

import java.util.List;
import java.util.Optional;

import org.jmolecules.ddd.annotation.Repository;

/**
 * Collection-like access to the instruments this system owns.
 *
 * <p>A driven port: the domain declares what it needs, an adapter in the infrastructure module
 * decides where the data actually lives.
 */
@Repository
public interface InstrumentRepository {

    Instrument save(Instrument instrument);

    Optional<Instrument> findBySymbol(Symbol symbol);

    Optional<Instrument> findByIsin(Isin isin);

    Optional<Instrument> findByWkn(Wkn wkn);

    /** All instruments matching a free-text query over symbol, name, ISIN and WKN. */
    List<Instrument> search(String query, int limit);

    List<Instrument> findAll();

    void delete(Symbol symbol);
}
