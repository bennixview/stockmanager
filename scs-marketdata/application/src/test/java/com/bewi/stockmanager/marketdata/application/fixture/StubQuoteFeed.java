package com.bewi.stockmanager.marketdata.application.fixture;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.marketdata.application.port.out.QuoteFeed;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/** A feed the test drives: it answers with what was put in and records what it was asked. */
public class StubQuoteFeed implements QuoteFeed, com.bewi.stockmanager.marketdata.application.port.out.PriceHistoryFeed {

    private final Map<Symbol, Money> prices = new HashMap<>();
    private final List<Symbol> requested = new ArrayList<>();
    private Instant asOf = Instant.parse("2026-05-04T12:00:00Z");
    private boolean broken;

    public StubQuoteFeed price(String symbol, String amount, String currency) {
        prices.put(Symbol.of(symbol), Money.of(amount, currency));
        return this;
    }

    public StubQuoteFeed asOf(Instant asOf) {
        this.asOf = asOf;
        return this;
    }

    /** Makes every call blow up, the way an unreachable provider does. */
    public StubQuoteFeed broken() {
        this.broken = true;
        return this;
    }

    public List<Symbol> requestedSymbols() {
        return List.copyOf(requested);
    }

    @Override
    public String name() {
        return "stub";
    }

    @Override
    public Optional<Quote> latestQuote(Instrument instrument) {
        if (broken) {
            throw new IllegalStateException("feed is down");
        }
        requested.add(instrument.symbol());
        return Optional.ofNullable(prices.get(instrument.symbol()))
                .map(price -> Quote.of(instrument.symbol(), price, price, asOf));
    }

    @Override
    public PriceHistory history(Instrument instrument, HistoryRange range) {
        if (broken) {
            throw new IllegalStateException("feed is down");
        }
        return PriceHistory.empty(instrument.symbol(), range);
    }
}
