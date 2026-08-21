package com.bewi.stockmanager.marketdata.application.service;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bewi.stockmanager.marketdata.application.port.in.QuoteListener;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteQuery;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteSubscriptions;
import com.bewi.stockmanager.marketdata.application.port.in.RefreshQuotes;
import com.bewi.stockmanager.marketdata.application.port.out.PriceHistoryFeed;
import com.bewi.stockmanager.marketdata.application.port.out.QuoteCache;
import com.bewi.stockmanager.marketdata.application.port.out.QuoteFeed;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Serves quotes from the cache and refreshes them from the feed.
 *
 * <p>Every read goes through the cache so that a page full of positions costs the upstream feed
 * nothing; the refresh cycle is what talks to the feed, driven from outside through
 * {@link RefreshQuotes}.
 */
public class QuoteService implements QuoteQuery, QuoteSubscriptions, RefreshQuotes {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final InstrumentRepository instruments;
    private final QuoteFeed feed;
    private final PriceHistoryFeed historyFeed;
    private final QuoteCache cache;
    private final Duration maxAge;
    private final Clock clock;
    private final Set<RegisteredSubscription> subscriptions = new CopyOnWriteArraySet<>();

    public QuoteService(InstrumentRepository instruments, QuoteFeed feed, PriceHistoryFeed historyFeed,
            QuoteCache cache, Duration maxAge, Clock clock) {
        this.instruments = Objects.requireNonNull(instruments);
        this.feed = Objects.requireNonNull(feed);
        this.historyFeed = Objects.requireNonNull(historyFeed);
        this.cache = Objects.requireNonNull(cache);
        this.maxAge = Objects.requireNonNull(maxAge);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Optional<Quote> quoteFor(Symbol symbol) {
        Optional<Quote> cached = cache.find(symbol);
        if (cached.filter(quote -> !quote.isStale(maxAge, clock.instant())).isPresent()) {
            return cached;
        }
        Optional<Quote> fetched = fetch(instrumentFor(symbol));
        fetched.ifPresent(cache::put);
        // A stale price beats no price at all when the feed is unavailable.
        return fetched.or(() -> cached);
    }

    @Override
    public Map<Symbol, Quote> quotesFor(Collection<Symbol> symbols) {
        Set<Symbol> wanted = new LinkedHashSet<>(symbols);
        Map<Symbol, Quote> result = new HashMap<>();
        Set<Symbol> outdated = new LinkedHashSet<>();

        Map<Symbol, Quote> cached = cache.findAll(wanted);
        for (Symbol symbol : wanted) {
            Quote quote = cached.get(symbol);
            if (quote == null || quote.isStale(maxAge, clock.instant())) {
                outdated.add(symbol);
            }
            if (quote != null) {
                result.put(symbol, quote);
            }
        }
        if (!outdated.isEmpty()) {
            Map<Symbol, Quote> fresh = fetchAll(outdated);
            fresh.values().forEach(cache::put);
            result.putAll(fresh);
        }
        return Map.copyOf(result);
    }

    @Override
    public PriceHistory historyFor(Symbol symbol, HistoryRange range) {
        try {
            return historyFeed.history(instrumentFor(symbol), range);
        } catch (RuntimeException e) {
            log.warn("History feed '{}' failed for {} ({}): {}", feed.name(), symbol, range, e.getMessage());
            return PriceHistory.empty(symbol, range);
        }
    }

    @Override
    public Subscription subscribe(Set<Symbol> symbols, QuoteListener listener) {
        RegisteredSubscription subscription = new RegisteredSubscription(Set.copyOf(symbols), listener);
        subscriptions.add(subscription);
        log.debug("New subscription for {} ({} active)", subscription.symbols(), subscriptions.size());
        // Hand out what we already know so a fresh client is not left staring at an empty widget.
        cache.findAll(subscription.symbols()).values().forEach(subscription::deliver);
        return subscription;
    }

    @Override
    public Set<Symbol> watchedSymbols() {
        return subscriptions.stream()
                .flatMap(subscription -> subscription.symbols().stream())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public int refreshWatchedQuotes() {
        Set<Symbol> watched = watchedSymbols();
        if (watched.isEmpty()) {
            return 0;
        }
        Map<Symbol, Quote> fresh = fetchAll(watched);
        int changed = 0;
        for (Quote quote : fresh.values()) {
            Optional<Quote> previous = cache.find(quote.symbol());
            cache.put(quote);
            if (previous.filter(quote::equals).isEmpty()) {
                changed++;
                notifySubscribers(quote);
            }
        }
        return changed;
    }

    private void notifySubscribers(Quote quote) {
        for (RegisteredSubscription subscription : subscriptions) {
            if (subscription.symbols().contains(quote.symbol())) {
                subscription.deliver(quote);
            }
        }
    }

    private Map<Symbol, Quote> fetchAll(Collection<Symbol> symbols) {
        List<Instrument> resolved = symbols.stream().map(this::instrumentFor).toList();
        try {
            return feed.latestQuotes(resolved);
        } catch (RuntimeException e) {
            log.warn("Quote feed '{}' failed for {}: {}", feed.name(), symbols, e.getMessage());
            return Map.of();
        }
    }

    private Optional<Quote> fetch(Instrument instrument) {
        try {
            return feed.latestQuote(instrument);
        } catch (RuntimeException e) {
            log.warn("Quote feed '{}' failed for {}: {}", feed.name(), instrument.symbol(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * The catalogue entry for a symbol, or a throw-away instrument so that anything the feed knows
     * can be priced without being registered first.
     */
    private Instrument instrumentFor(Symbol symbol) {
        return instruments.findBySymbol(symbol).orElseGet(() -> Instrument.builder(symbol).build());
    }

    private final class RegisteredSubscription implements Subscription {

        private final Set<Symbol> symbols;
        private final QuoteListener listener;

        private RegisteredSubscription(Set<Symbol> symbols, QuoteListener listener) {
            this.symbols = symbols;
            this.listener = Objects.requireNonNull(listener);
        }

        @Override
        public Set<Symbol> symbols() {
            return symbols;
        }

        private void deliver(Quote quote) {
            try {
                listener.onQuote(quote);
            } catch (RuntimeException e) {
                log.debug("Subscriber rejected quote for {}, dropping subscription: {}", quote.symbol(), e.getMessage());
                close();
            }
        }

        @Override
        public void close() {
            subscriptions.remove(this);
        }
    }
}
