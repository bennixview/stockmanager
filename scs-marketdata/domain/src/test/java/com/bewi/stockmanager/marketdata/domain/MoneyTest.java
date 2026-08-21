package com.bewi.stockmanager.marketdata.domain;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void addsAmountsOfTheSameCurrency() {
        assertThat(Money.of("10.50", "EUR").plus(Money.of("4.50", "EUR")))
                .isEqualTo(Money.of("15.00", "EUR"));
    }

    @Test
    void refusesToMixCurrencies() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Money.of("10", "EUR").plus(Money.of("10", "USD")))
                .withMessageContaining("EUR")
                .withMessageContaining("USD");
    }

    @Test
    void comparesEqualRegardlessOfTrailingZeros() {
        assertThat(Money.of("10", "EUR")).isEqualTo(Money.of("10.000000", "EUR"));
    }

    @Test
    void computesRelativeChange() {
        assertThat(Money.of("100", "EUR").percentageChangeTo(Money.of("110", "EUR")))
                .isEqualByComparingTo("10");
        assertThat(Money.of("100", "EUR").percentageChangeTo(Money.of("95", "EUR")))
                .isEqualByComparingTo("-5");
    }

    @Test
    void treatsChangeFromZeroAsNoChangeRatherThanDividingByZero() {
        assertThat(Money.zero(EUR).percentageChangeTo(Money.of("10", "EUR"))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void roundsToTheCurrencysFractionDigitsForDisplay() {
        assertThat(Money.of("10.126", "EUR").rounded()).isEqualByComparingTo("10.13");
        assertThat(Money.of(10.126, USD).toString()).isEqualTo("10.13 USD");
    }
}
