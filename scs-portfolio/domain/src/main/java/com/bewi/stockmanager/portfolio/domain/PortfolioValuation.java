package com.bewi.stockmanager.portfolio.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The portfolio's totals, in its base currency.
 *
 * @param unpricedPositions positions left out of the totals because no price or no exchange rate
 *                          was available - shown in the UI rather than silently swallowed
 */
@ValueObject
public record PortfolioValuation(
        PortfolioId portfolioId,
        Currency baseCurrency,
        Money invested,
        Money marketValue,
        Money unrealizedGain,
        BigDecimal unrealizedGainPercent,
        Money realizedGain,
        List<PositionValuation> positions,
        List<PositionValuation> unpricedPositions) {

    public PortfolioValuation {
        Objects.requireNonNull(portfolioId, "portfolioId must not be null");
        Objects.requireNonNull(baseCurrency, "baseCurrency must not be null");
        positions = List.copyOf(positions);
        unpricedPositions = List.copyOf(unpricedPositions);
    }

    /** Total value including profits already banked. */
    public Money totalReturn() {
        return unrealizedGain.plus(realizedGain);
    }

    public boolean isComplete() {
        return unpricedPositions.isEmpty();
    }

    public boolean isUp() {
        return !unrealizedGain.isNegative();
    }
}
