package com.bewi.stockmanager.portfolio.application.fixture;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/** A market data system the test controls, including the case where it is simply not there. */
public class StubMarketData implements MarketDataPort {

    private final Map<Symbol, InstrumentRef> instruments = new LinkedHashMap<>();
    private final Map<Symbol, Money> prices = new HashMap<>();
    private boolean unavailable;

    public StubMarketData knows(InstrumentRef instrument) {
        instruments.put(instrument.symbol(), instrument);
        return this;
    }

    public StubMarketData prices(Symbol symbol, String amount, String currency) {
        prices.put(symbol, Money.of(amount, currency));
        return this;
    }

    /** Simulates the other system being down: every answer is empty. */
    public StubMarketData unavailable() {
        this.unavailable = true;
        return this;
    }

    @Override
    public Optional<MarketPrice> priceFor(Symbol symbol) {
        if (unavailable) {
            return Optional.empty();
        }
        return Optional.ofNullable(prices.get(symbol))
                .map(price -> new MarketPrice(symbol, price, BigDecimal.ZERO, Instant.EPOCH));
    }

    @Override
    public Map<Symbol, MarketPrice> pricesFor(Collection<Symbol> symbols) {
        Map<Symbol, MarketPrice> found = new HashMap<>();
        symbols.forEach(symbol -> priceFor(symbol).ifPresent(price -> found.put(symbol, price)));
        return found;
    }

    @Override
    public List<InstrumentRef> search(String query, int limit) {
        if (unavailable) {
            return List.of();
        }
        return instruments.values().stream()
                .filter(instrument -> instrument.name().toLowerCase().contains(query.toLowerCase())
                        || instrument.symbol().value().contains(query.toUpperCase()))
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<InstrumentRef> bySymbol(Symbol symbol) {
        return unavailable ? Optional.empty() : Optional.ofNullable(instruments.get(symbol));
    }

    @Override
    public Optional<InstrumentRef> byWkn(Wkn wkn) {
        if (unavailable) {
            return Optional.empty();
        }
        return instruments.values().stream()
                .filter(instrument -> instrument.wknValue().filter(wkn::equals).isPresent())
                .findFirst();
    }

    public static InstrumentRef instrument(String symbol, String name, String currency) {
        return InstrumentRef.of(Symbol.of(symbol), name, Currency.getInstance(currency));
    }
}
