package com.bewi.stockmanager.marketdata.application.fixture;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.bewi.stockmanager.marketdata.application.port.out.QuoteCache;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/** Cache port backed by a map, with no eviction - the test decides what is in it. */
public class InMemoryQuoteCache implements QuoteCache {

    private final Map<Symbol, Quote> quotes = new ConcurrentHashMap<>();

    @Override
    public Optional<Quote> find(Symbol symbol) {
        return Optional.ofNullable(quotes.get(symbol));
    }

    @Override
    public Map<Symbol, Quote> findAll(Collection<Symbol> symbols) {
        Map<Symbol, Quote> found = new HashMap<>();
        symbols.forEach(symbol -> find(symbol).ifPresent(quote -> found.put(symbol, quote)));
        return found;
    }

    @Override
    public void put(Quote quote) {
        quotes.put(quote.symbol(), quote);
    }

    @Override
    public void clear() {
        quotes.clear();
    }
}
