package com.bewi.stockmanager.marketdata.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/** Driving port: ask this system what an instrument is worth right now. */
public interface QuoteQuery {

    /** The latest quote for a symbol, or empty if the symbol is unknown and no feed can price it. */
    Optional<Quote> quoteFor(Symbol symbol);

    /** Latest quotes for several symbols at once; symbols that cannot be priced are left out. */
    Map<Symbol, Quote> quotesFor(Collection<Symbol> symbols);

    PriceHistory historyFor(Symbol symbol, HistoryRange range);
}
