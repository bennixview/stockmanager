package com.bewi.stockmanager.portfolio.adapter.out.marketdata;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

import static org.assertj.core.api.Assertions.assertThat;

class MarketDataExchangeRatesTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-04T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void convertsUsingTheCurrencyPairQuoteOfTheMarketDataSystem() {
        CountingMarketData marketData = new CountingMarketData(Map.of(Symbol.of("USDEUR=X"), "0.90"));
        ExchangeRates rates = new MarketDataExchangeRates(marketData, Duration.ofMinutes(5), CLOCK);

        assertThat(rates.convert(Money.of("100", "USD"), EUR)).contains(Money.of("90", "EUR"));
    }

    @Test
    void passesAnAmountThroughThatIsAlreadyInTheTargetCurrency() {
        CountingMarketData marketData = new CountingMarketData(Map.of());
        ExchangeRates rates = new MarketDataExchangeRates(marketData, Duration.ofMinutes(5), CLOCK);

        assertThat(rates.convert(Money.of("100", "EUR"), EUR)).contains(Money.of("100", "EUR"));
        assertThat(marketData.calls()).isZero();
    }

    @Test
    void reusesAFetchedRateWithinItsLifetime() {
        CountingMarketData marketData = new CountingMarketData(Map.of(Symbol.of("USDEUR=X"), "0.90"));
        ExchangeRates rates = new MarketDataExchangeRates(marketData, Duration.ofMinutes(5), CLOCK);

        rates.convert(Money.of("100", "USD"), EUR);
        rates.convert(Money.of("200", "USD"), EUR);

        assertThat(marketData.calls()).isEqualTo(1);
    }

    @Test
    void answersEmptyWhenNoRateCanBeHad() {
        ExchangeRates rates = new MarketDataExchangeRates(new CountingMarketData(Map.of()), Duration.ofMinutes(5),
                CLOCK);

        assertThat(rates.convert(Money.of("100", "USD"), EUR)).isEmpty();
    }

    @Test
    void convertsZeroWithoutAskingForARate() {
        CountingMarketData marketData = new CountingMarketData(Map.of());
        ExchangeRates rates = new MarketDataExchangeRates(marketData, Duration.ofMinutes(5), CLOCK);

        assertThat(rates.convert(Money.zero(USD), EUR)).contains(Money.zero(EUR));
        assertThat(marketData.calls()).isZero();
    }

    /** Market data stand-in that answers with fixed rates and counts how often it was asked. */
    private static final class CountingMarketData implements MarketDataPort {

        private final Map<Symbol, String> rates;
        private int calls;

        private CountingMarketData(Map<Symbol, String> rates) {
            this.rates = rates;
        }

        int calls() {
            return calls;
        }

        @Override
        public Optional<MarketPrice> priceFor(Symbol symbol) {
            calls++;
            return Optional.ofNullable(rates.get(symbol))
                    .map(rate -> new MarketPrice(symbol, Money.of(rate, "EUR"), BigDecimal.ZERO, Instant.EPOCH));
        }

        @Override
        public Map<Symbol, MarketPrice> pricesFor(Collection<Symbol> symbols) {
            return Map.of();
        }

        @Override
        public List<InstrumentRef> search(String query, int limit) {
            return List.of();
        }

        @Override
        public Optional<InstrumentRef> bySymbol(Symbol symbol) {
            return Optional.empty();
        }

        @Override
        public Optional<InstrumentRef> byWkn(Wkn wkn) {
            return Optional.empty();
        }
    }
}
