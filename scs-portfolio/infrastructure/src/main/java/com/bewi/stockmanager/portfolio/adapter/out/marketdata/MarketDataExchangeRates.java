package com.bewi.stockmanager.portfolio.adapter.out.marketdata;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/**
 * Converts currencies by asking the market data system for the FX pair.
 *
 * <p>An exchange rate is just another quote - {@code USDEUR=X} in the ticker convention the market
 * data system uses - so this needs no second integration. Rates are cached for a few minutes
 * because a portfolio total does not need them to the second.
 */
public class MarketDataExchangeRates implements ExchangeRates {

    private static final Logger log = LoggerFactory.getLogger(MarketDataExchangeRates.class);

    private final MarketDataPort marketData;
    private final Duration timeToLive;
    private final Clock clock;
    private final Map<String, CachedRate> rates = new ConcurrentHashMap<>();

    public MarketDataExchangeRates(MarketDataPort marketData, Duration timeToLive, Clock clock) {
        this.marketData = marketData;
        this.timeToLive = timeToLive;
        this.clock = clock;
    }

    @Override
    public Optional<Money> convert(Money amount, Currency target) {
        if (amount.currency().equals(target)) {
            return Optional.of(amount);
        }
        if (amount.isZero()) {
            return Optional.of(Money.zero(target));
        }
        return rate(amount.currency(), target).map(rate -> Money.of(amount.amount().multiply(rate), target));
    }

    private Optional<BigDecimal> rate(Currency from, Currency to) {
        String pair = from.getCurrencyCode() + to.getCurrencyCode();
        CachedRate cached = rates.get(pair);
        Instant now = clock.instant();
        if (cached != null && cached.fetchedAt().plus(timeToLive).isAfter(now)) {
            return Optional.of(cached.rate());
        }
        Optional<BigDecimal> fetched = marketData.priceFor(Symbol.of(pair + "=X"))
                .map(price -> price.price().amount());
        fetched.ifPresentOrElse(
                rate -> rates.put(pair, new CachedRate(rate, now)),
                () -> log.debug("No exchange rate available for {}", pair));
        // Fall back to the last rate we saw rather than dropping the position out of the total.
        return fetched.or(() -> Optional.ofNullable(cached).map(CachedRate::rate));
    }

    private record CachedRate(BigDecimal rate, Instant fetchedAt) {
    }
}
