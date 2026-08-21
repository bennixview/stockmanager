package com.bewi.stockmanager.marketdata.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bewi.stockmanager.marketdata.application.fixture.InMemoryInstrumentRepository;
import com.bewi.stockmanager.marketdata.application.fixture.InMemoryQuoteCache;
import com.bewi.stockmanager.marketdata.application.fixture.StubQuoteFeed;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteSubscriptions;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentType;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceTest {

    private static final Symbol AAPL = Symbol.of("AAPL");
    private static final Instant NOW = Instant.parse("2026-05-04T12:00:00Z");

    private MutableClock clock;
    private InMemoryInstrumentRepository instruments;
    private InMemoryQuoteCache cache;
    private StubQuoteFeed feed;
    private QuoteService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        instruments = new InMemoryInstrumentRepository();
        instruments.save(Instrument.builder(AAPL).name("Apple Inc.").currency("USD").type(InstrumentType.STOCK)
                .build());
        cache = new InMemoryQuoteCache();
        feed = new StubQuoteFeed().price("AAPL", "190.00", "USD").asOf(NOW);
        service = new QuoteService(instruments, feed, feed, cache, Duration.ofSeconds(30), clock);
    }

    @Test
    void fetchesFromTheFeedAndCachesTheResult() {
        assertThat(service.quoteFor(AAPL)).hasValueSatisfying(
                quote -> assertThat(quote.price()).isEqualTo(Money.of("190.00", "USD")));
        assertThat(cache.find(AAPL)).isPresent();
    }

    @Test
    void servesTheCacheWhileTheQuoteIsFresh() {
        service.quoteFor(AAPL);
        service.quoteFor(AAPL);

        assertThat(feed.requestedSymbols()).containsExactly(AAPL);
    }

    @Test
    void refetchesOnceTheCachedQuoteIsStale() {
        service.quoteFor(AAPL);
        clock.advance(Duration.ofSeconds(31));
        feed.asOf(clock.instant());

        service.quoteFor(AAPL);

        assertThat(feed.requestedSymbols()).containsExactly(AAPL, AAPL);
    }

    @Test
    void fallsBackToAStalePriceWhenTheFeedIsDown() {
        service.quoteFor(AAPL);
        clock.advance(Duration.ofHours(1));
        StubQuoteFeed brokenFeed = new StubQuoteFeed().broken();
        QuoteService withBrokenFeed = new QuoteService(instruments, brokenFeed, brokenFeed, cache,
                Duration.ofSeconds(30), clock);

        assertThat(withBrokenFeed.quoteFor(AAPL)).hasValueSatisfying(
                quote -> assertThat(quote.price()).isEqualTo(Money.of("190.00", "USD")));
    }

    @Test
    void answersWithNothingForASymbolNoOneCanPrice() {
        assertThat(service.quoteFor(Symbol.of("NOSUCH"))).isEmpty();
    }

    @Test
    void pricesSymbolsThatAreNotInTheCatalogue() {
        feed.price("TSLA", "250.00", "EUR");

        assertThat(service.quoteFor(Symbol.of("TSLA"))).isPresent();
    }

    @Test
    void deliversTheKnownPriceToANewSubscriberImmediately() {
        service.quoteFor(AAPL);
        List<Quote> received = new ArrayList<>();

        service.subscribe(Set.of(AAPL), received::add);

        assertThat(received).hasSize(1);
    }

    @Test
    void pushesChangedPricesToSubscribersOnRefresh() {
        List<Quote> received = new ArrayList<>();
        service.subscribe(Set.of(AAPL), received::add);

        int updated = service.refreshWatchedQuotes();

        assertThat(updated).isEqualTo(1);
        assertThat(received).hasSize(1);
        assertThat(received.getFirst().price()).isEqualTo(Money.of("190.00", "USD"));
    }

    @Test
    void staysQuietWhenNothingChanged() {
        service.subscribe(Set.of(AAPL), quote -> {
        });
        service.refreshWatchedQuotes();

        assertThat(service.refreshWatchedQuotes()).isZero();
    }

    @Test
    void pollsNothingWhileNoOneIsWatching() {
        assertThat(service.refreshWatchedQuotes()).isZero();
        assertThat(feed.requestedSymbols()).isEmpty();
    }

    @Test
    void stopsWatchingOnceTheSubscriptionIsClosed() {
        QuoteSubscriptions.Subscription subscription = service.subscribe(Set.of(AAPL), quote -> {
        });
        assertThat(service.watchedSymbols()).containsExactly(AAPL);

        subscription.close();

        assertThat(service.watchedSymbols()).isEmpty();
    }

    @Test
    void dropsASubscriberThatCannotTakeTheQuoteAnyMore() {
        service.subscribe(Set.of(AAPL), quote -> {
            throw new IllegalStateException("client is gone");
        });

        service.refreshWatchedQuotes();

        assertThat(service.watchedSymbols()).isEmpty();
    }

    @Test
    void batchesRequestsForSeveralSymbols() {
        feed.price("MSFT", "410.00", "USD");

        Map<Symbol, Quote> quotes = service.quotesFor(List.of(AAPL, Symbol.of("MSFT"), Symbol.of("NOSUCH")));

        assertThat(quotes).containsOnlyKeys(AAPL, Symbol.of("MSFT"));
    }

    /** A clock the test moves forward, so staleness can be exercised without sleeping. */
    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
