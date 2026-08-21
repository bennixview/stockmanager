package com.bewi.stockmanager.marketdata.adapter.out.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.bewi.stockmanager.marketdata.application.port.out.QuoteCache;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * In-memory quote cache.
 *
 * <p>Quotes are worthless once they are old, so losing them on restart is fine and an external
 * cache would only add an operational dependency.
 */
public class CaffeineQuoteCache implements QuoteCache {

    private final Cache<Symbol, Quote> cache;

    public CaffeineQuoteCache(int maximumSize, java.time.Duration timeToLive) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(timeToLive)
                .build();
    }

    @Override
    public Optional<Quote> find(Symbol symbol) {
        return Optional.ofNullable(cache.getIfPresent(symbol));
    }

    @Override
    public Map<Symbol, Quote> findAll(Collection<Symbol> symbols) {
        return Map.copyOf(cache.getAllPresent(symbols));
    }

    @Override
    public void put(Quote quote) {
        cache.put(quote.symbol(), quote);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
