package com.bewi.stockmanager.marketdata.domain;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class QuoteTest {

    private static final Instant NOON = Instant.parse("2026-05-04T12:00:00Z");

    @Test
    void reportsTheMoveSinceThePreviousClose() {
        Quote quote = Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), Money.of("100", "USD"), NOON);

        assertThat(quote.change()).isEqualTo(Money.of("10", "USD"));
        assertThat(quote.changePercent()).isEqualByComparingTo("10");
        assertThat(quote.isUp()).isTrue();
    }

    @Test
    void treatsAMissingPreviousCloseAsNoMove() {
        Quote quote = Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), null, NOON);

        assertThat(quote.change().isZero()).isTrue();
        assertThat(quote.changePercent()).isEqualByComparingTo("0");
    }

    @Test
    void knowsWhenItHasGoneStale() {
        Quote quote = Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), null, NOON);

        assertThat(quote.isStale(Duration.ofMinutes(5), NOON.plusSeconds(299))).isFalse();
        assertThat(quote.isStale(Duration.ofMinutes(5), NOON.plusSeconds(301))).isTrue();
    }

    @Test
    void refusesAPreviousCloseInAnotherCurrency() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), Money.of("100", "EUR"), NOON));
    }
}
