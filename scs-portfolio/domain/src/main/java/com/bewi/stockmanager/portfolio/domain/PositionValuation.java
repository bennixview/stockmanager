package com.bewi.stockmanager.portfolio.domain;

import java.math.BigDecimal;
import java.util.Optional;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * What one position is worth right now.
 *
 * <p>Everything derived from a market price is optional, because a position stays perfectly valid
 * when the market data system cannot price it.
 */
@ValueObject
public record PositionValuation(
        PositionId positionId,
        InstrumentRef instrument,
        Quantity quantity,
        Money averagePrice,
        Money currentPrice,
        Money invested,
        Money marketValue,
        Money unrealizedGain,
        BigDecimal unrealizedGainPercent,
        Money realizedGain) {

    public boolean isPriced() {
        return currentPrice != null && marketValue != null;
    }

    public Optional<Money> marketValueIfPriced() {
        return Optional.ofNullable(marketValue);
    }

    public Optional<Money> unrealizedGainIfPriced() {
        return Optional.ofNullable(unrealizedGain);
    }

    public boolean isUp() {
        return unrealizedGain != null && !unrealizedGain.isNegative();
    }
}
