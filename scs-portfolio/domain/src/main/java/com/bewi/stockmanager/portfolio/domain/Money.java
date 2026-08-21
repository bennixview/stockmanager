package com.bewi.stockmanager.portfolio.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A monetary amount in a single currency.
 *
 * <p>Deliberately duplicated in every self-contained system: sharing business code across systems
 * would couple their release cycles, which is exactly what SCS avoids.
 */
@ValueObject
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final int SCALE = 6;

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        return new Money(amount.add(sameCurrency(other).amount), currency);
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(sameCurrency(other).amount), currency);
    }

    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    /**
     * The relative change from this amount to {@code other}, in percent.
     *
     * @return {@code null}-free result; {@link BigDecimal#ZERO} if this amount is zero
     */
    public BigDecimal percentageChangeTo(Money other) {
        sameCurrency(other);
        if (isZero()) {
            return BigDecimal.ZERO;
        }
        return other.amount.subtract(amount)
                .multiply(BigDecimal.valueOf(100))
                .divide(amount, 4, RoundingMode.HALF_UP);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /** The amount rounded to the currency's default fraction digits, for display and transport. */
    public BigDecimal rounded() {
        return amount.setScale(Math.max(currency.getDefaultFractionDigits(), 0), RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(sameCurrency(other).amount);
    }

    @Override
    public String toString() {
        return rounded().toPlainString() + " " + currency.getCurrencyCode();
    }

    private Money sameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot combine %s with %s".formatted(currency.getCurrencyCode(), other.currency.getCurrencyCode()));
        }
        return other;
    }
}
