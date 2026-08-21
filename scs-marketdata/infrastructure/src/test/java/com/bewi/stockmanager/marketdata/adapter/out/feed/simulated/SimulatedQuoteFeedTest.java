package com.bewi.stockmanager.marketdata.adapter.out.feed.simulated;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

/** The offline feed has to behave like a feed, or demos and tests lie about the real thing. */
class SimulatedQuoteFeedTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-04T12:00:00Z"), ZoneOffset.UTC);
    private final SimulatedQuoteFeed feed = new SimulatedQuoteFeed(clock);

    private final Instrument apple = Instrument.builder(Symbol.of("AAPL")).name("Apple").currency("USD").build();

    @Test
    void pricesAnythingItIsAsked() {
        assertThat(feed.latestQuote(apple)).isPresent();
    }

    @Test
    void quotesInTheInstrumentsCurrency() {
        Quote quote = feed.latestQuote(apple).orElseThrow();

        assertThat(quote.price().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(quote.price().amount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void staysInTheSamePriceRegionForASymbolAcrossRestarts() {
        BigDecimal first = feed.latestQuote(apple).orElseThrow().price().amount();
        BigDecimal afterRestart = new SimulatedQuoteFeed(clock).latestQuote(apple).orElseThrow().price().amount();

        assertThat(afterRestart).isCloseTo(first, org.assertj.core.data.Percentage.withPercentage(5));
    }

    @Test
    void producesADifferentPriceRegionPerSymbol() {
        Instrument microsoft = Instrument.builder(Symbol.of("MSFT")).name("Microsoft").currency("USD").build();

        assertThat(feed.latestQuote(apple).orElseThrow().price())
                .isNotEqualTo(feed.latestQuote(microsoft).orElseThrow().price());
    }

    @Test
    void quotesACurrencyPairAsARateInItsSecondCurrency() {
        Instrument dollarInEuro = Instrument.builder(Symbol.of("USDEUR=X")).name("US Dollar / Euro").build();

        Quote quote = feed.latestQuote(dollarInEuro).orElseThrow();

        assertThat(quote.price().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(quote.price().amount()).isBetween(new BigDecimal("0.4"), new BigDecimal("1.6"));
    }

    @Test
    void producesAHistoryThatFitsTheRequestedRange() {
        PriceHistory history = feed.history(apple, HistoryRange.MONTH);

        assertThat(history.candles()).hasSizeGreaterThan(10);
        assertThat(history.range()).isEqualTo(HistoryRange.MONTH);
        assertThat(history.first().orElseThrow().at()).isBefore(history.last().orElseThrow().at());
        assertThat(history.last().orElseThrow().at()).isBeforeOrEqualTo(clock.instant());
    }

    @Test
    void producesCandlesWhoseHighAndLowBracketOpenAndClose() {
        feed.history(apple, HistoryRange.WEEK).candles().forEach(candle -> {
            assertThat(candle.high()).isGreaterThanOrEqualTo(candle.open().max(candle.close()));
            assertThat(candle.low()).isLessThanOrEqualTo(candle.open().min(candle.close()));
        });
    }
}
