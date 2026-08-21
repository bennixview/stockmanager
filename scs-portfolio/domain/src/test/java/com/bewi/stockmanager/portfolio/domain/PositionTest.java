package com.bewi.stockmanager.portfolio.domain;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PositionTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 15);
    private static final LocalDate MARCH = LocalDate.of(2026, 3, 15);
    private static final LocalDate JUNE = LocalDate.of(2026, 6, 15);

    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position(PositionId.generate(),
                InstrumentRef.of(Symbol.of("SAP.DE"), "SAP SE", EUR));
    }

    @Test
    void addsUpTheSharesOfEveryTranche() {
        position.buy(Quantity.of(10), euro("100"), euro("5"), JANUARY);
        position.buy(Quantity.of(5), euro("120"), euro("5"), MARCH);

        assertThat(position.quantity()).isEqualTo(Quantity.of(15));
    }

    @Test
    void countsFeesAsPartOfWhatWasInvested() {
        position.buy(Quantity.of(10), euro("100"), euro("9.90"), JANUARY);

        assertThat(position.invested()).isEqualTo(euro("1009.90"));
        assertThat(position.averagePrice()).isEqualTo(euro("100.99"));
    }

    @Test
    void supportsFractionalShares() {
        position.buy(Quantity.of("0.5"), euro("200"), euro("0"), JANUARY);

        assertThat(position.quantity()).isEqualTo(Quantity.of("0.5"));
        assertThat(position.invested()).isEqualTo(euro("100"));
    }

    @Test
    void sellsTheOldestTrancheFirst() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);
        position.buy(Quantity.of(10), euro("150"), euro("0"), MARCH);

        List<RealizedTrade> closed = position.sell(Quantity.of(10), euro("200"), euro("0"), JUNE);

        assertThat(closed).hasSize(1);
        assertThat(closed.getFirst().buyPricePerShare()).isEqualTo(euro("100"));
        assertThat(closed.getFirst().gain()).isEqualTo(euro("1000"));
        assertThat(position.quantity()).isEqualTo(Quantity.of(10));
        assertThat(position.tranches()).singleElement()
                .satisfies(tranche -> assertThat(tranche.pricePerShare()).isEqualTo(euro("150")));
    }

    @Test
    void spreadsASaleOverAsManyTranchesAsItTakes() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);
        position.buy(Quantity.of(10), euro("150"), euro("0"), MARCH);

        List<RealizedTrade> closed = position.sell(Quantity.of(15), euro("200"), euro("0"), JUNE);

        assertThat(closed).hasSize(2);
        assertThat(closed).extracting(trade -> trade.quantity().value().intValue()).containsExactly(10, 5);
        assertThat(position.quantity()).isEqualTo(Quantity.of(5));
    }

    @Test
    void keepsTheRestOfAPartlySoldTranche() {
        position.buy(Quantity.of(10), euro("100"), euro("10"), JANUARY);

        position.sell(Quantity.of(4), euro("120"), euro("0"), JUNE);

        assertThat(position.quantity()).isEqualTo(Quantity.of(6));
        // The buy fee follows the shares: six of ten shares carry six euros of it.
        assertThat(position.invested()).isEqualTo(euro("606"));
    }

    @Test
    void splitsFeesBetweenTheLotsASaleTouches() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);
        position.buy(Quantity.of(10), euro("100"), euro("0"), MARCH);

        List<RealizedTrade> closed = position.sell(Quantity.of(20), euro("110"), euro("10"), JUNE);

        assertThat(closed).extracting(RealizedTrade::fees).containsExactly(euro("5"), euro("5"));
    }

    @Test
    void addsUpWhatWasRealized() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);
        position.sell(Quantity.of(5), euro("120"), euro("0"), JUNE);
        position.sell(Quantity.of(5), euro("90"), euro("0"), JUNE);

        assertThat(position.realizedGain()).isEqualTo(euro("50"));
        assertThat(position.isClosed()).isTrue();
    }

    @Test
    void refusesToSellMoreThanItHolds() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);

        assertThatExceptionOfType(InsufficientSharesException.class)
                .isThrownBy(() -> position.sell(Quantity.of(11), euro("100"), euro("0"), JUNE))
                .satisfies(e -> {
                    assertThat(e.held()).isEqualTo(Quantity.of(10));
                    assertThat(e.requested()).isEqualTo(Quantity.of(11));
                });
        assertThat(position.quantity()).isEqualTo(Quantity.of(10));
    }

    @Test
    void refusesTradesInAnotherCurrencyThanTheInstrumentIsQuotedIn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> position.buy(Quantity.of(1), Money.of("100", "USD"), null, JANUARY))
                .withMessageContaining("EUR");
    }

    @Test
    void valuesItselfAtTheCurrentPrice() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);

        PositionValuation valuation = position.valuate(euro("125"));

        assertThat(valuation.marketValue()).isEqualTo(euro("1250"));
        assertThat(valuation.unrealizedGain()).isEqualTo(euro("250"));
        assertThat(valuation.unrealizedGainPercent()).isEqualByComparingTo("25");
        assertThat(valuation.isPriced()).isTrue();
        assertThat(valuation.isUp()).isTrue();
    }

    @Test
    void stillValuesWhatItCostWhenNoPriceIsAvailable() {
        position.buy(Quantity.of(10), euro("100"), euro("0"), JANUARY);

        PositionValuation valuation = position.valuateWithoutPrice();

        assertThat(valuation.isPriced()).isFalse();
        assertThat(valuation.invested()).isEqualTo(euro("1000"));
        assertThat(valuation.marketValue()).isNull();
    }

    private static Money euro(String amount) {
        return Money.of(amount, "EUR");
    }
}
