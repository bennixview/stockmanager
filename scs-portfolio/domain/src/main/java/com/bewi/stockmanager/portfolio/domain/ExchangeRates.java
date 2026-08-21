package com.bewi.stockmanager.portfolio.domain;

import java.util.Currency;
import java.util.Optional;

/**
 * Driven port for converting between currencies.
 *
 * <p>A portfolio in euros holding a share priced in dollars can only be totalled with a rate, and
 * where that rate comes from is not the domain's business. Returning an empty result is a normal
 * outcome: the valuation then reports the position as unpriced instead of inventing a number.
 */
@FunctionalInterface
public interface ExchangeRates {

    /** Converts an amount into {@code target}, or empty when no rate is available. */
    Optional<Money> convert(Money amount, Currency target);

    /** An implementation that only passes amounts through that already are in the target currency. */
    static ExchangeRates none() {
        return (amount, target) -> amount.currency().equals(target) ? Optional.of(amount) : Optional.empty();
    }
}
