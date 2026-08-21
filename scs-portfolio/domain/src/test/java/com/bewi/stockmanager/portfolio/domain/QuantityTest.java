package com.bewi.stockmanager.portfolio.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class QuantityTest {

    @Test
    void addsAndSubtracts() {
        assertThat(Quantity.of(10).plus(Quantity.of("2.5"))).isEqualTo(Quantity.of("12.5"));
        assertThat(Quantity.of(10).minus(Quantity.of("2.5"))).isEqualTo(Quantity.of("7.5"));
    }

    @Test
    void refusesToGoNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> Quantity.of(1).minus(Quantity.of(2)));
        assertThatIllegalArgumentException().isThrownBy(() -> Quantity.of("-1"));
    }

    @Test
    void multipliesOutToAnAmount() {
        assertThat(Quantity.of("2.5").at(Money.of("100", "EUR"))).isEqualTo(Money.of("250", "EUR"));
    }

    @Test
    void ignoresTrailingZerosWhenComparing() {
        assertThat(Quantity.of("10.000000")).isEqualTo(Quantity.of(10));
        assertThat(Quantity.of("10.5").toString()).isEqualTo("10.5");
    }
}
