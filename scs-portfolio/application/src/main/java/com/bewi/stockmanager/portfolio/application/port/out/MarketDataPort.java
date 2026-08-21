package com.bewi.stockmanager.portfolio.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/**
 * Driven port to the market data system.
 *
 * <p>The only way this system learns about prices and instruments. It is deliberately narrow: what
 * crosses the boundary between two self-contained systems should be as small as the job allows.
 * Every method degrades to an empty result, because the other system being unavailable must never
 * stop a portfolio from being read or a trade from being booked.
 */
public interface MarketDataPort {

    Optional<MarketPrice> priceFor(Symbol symbol);

    Map<Symbol, MarketPrice> pricesFor(Collection<Symbol> symbols);

    List<InstrumentRef> search(String query, int limit);

    Optional<InstrumentRef> bySymbol(Symbol symbol);

    Optional<InstrumentRef> byWkn(Wkn wkn);

    /** A price with the day's move, as reported by the market data system. */
    record MarketPrice(Symbol symbol, Money price, BigDecimal changePercent, Instant asOf) {

        public boolean isUp() {
            return changePercent == null || changePercent.signum() >= 0;
        }
    }
}
