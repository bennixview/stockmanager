package com.bewi.stockmanager.marketdata.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Driven port for the last known price per symbol.
 *
 * <p>It is a port rather than a plain map because it is what shields the upstream feed from the
 * request rate of the UI, and because the eviction policy is an infrastructure decision.
 */
public interface QuoteCache {

    Optional<Quote> find(Symbol symbol);

    Map<Symbol, Quote> findAll(Collection<Symbol> symbols);

    void put(Quote quote);

    void clear();
}
