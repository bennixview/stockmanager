package com.bewi.stockmanager.marketdata.application.port.out;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Driven port to the outside world that actually knows prices.
 *
 * <p>Implementations are expected to be slow and unreliable: they answer with {@link Optional#empty()}
 * rather than throwing when a price cannot be obtained.
 */
public interface QuoteFeed {

    /** Name of the feed, for logging and for showing the data source in the UI. */
    String name();

    Optional<Quote> latestQuote(Instrument instrument);

    /** Batch variant; the default fetches one by one and is fine for feeds without a bulk endpoint. */
    default Map<Symbol, Quote> latestQuotes(Collection<Instrument> instruments) {
        Map<Symbol, Quote> quotes = new HashMap<>();
        for (Instrument instrument : instruments) {
            latestQuote(instrument).ifPresent(quote -> quotes.put(quote.symbol(), quote));
        }
        return quotes;
    }
}
