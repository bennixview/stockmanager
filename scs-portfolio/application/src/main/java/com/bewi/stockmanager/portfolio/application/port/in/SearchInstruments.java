package com.bewi.stockmanager.portfolio.application.port.in;

import java.util.List;
import java.util.Optional;

import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/** Driving port for finding something to buy. Answers come from the market data system. */
public interface SearchInstruments {

    List<InstrumentRef> search(String query);

    Optional<InstrumentRef> resolve(Symbol symbol);
}
