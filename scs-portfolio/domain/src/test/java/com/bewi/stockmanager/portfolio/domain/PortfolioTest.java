package com.bewi.stockmanager.portfolio.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PortfolioTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 15);
    private static final LocalDate JUNE = LocalDate.of(2026, 6, 15);
    private static final Symbol SAP = Symbol.of("SAP.DE");
    private static final Symbol AAPL = Symbol.of("AAPL");

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = Portfolio.open("Mein Depot", EUR, Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void opensOnePositionPerInstrumentAndExtendsItOnTheNextBuy() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);
        portfolio.buy(sap(), Quantity.of(5), euro("120"), euro("0"), JUNE);

        assertThat(portfolio.positions()).hasSize(1);
        assertThat(portfolio.positions().getFirst().quantity()).isEqualTo(Quantity.of(15));
        assertThat(portfolio.positions().getFirst().tranches()).hasSize(2);
    }

    @Test
    void keepsInstrumentsApart() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);
        portfolio.buy(apple(), Quantity.of(2), Money.of("200", "USD"), null, JANUARY);

        assertThat(portfolio.positions()).hasSize(2);
        assertThat(portfolio.watchedSymbols()).containsExactlyInAnyOrder(SAP, AAPL);
    }

    @Test
    void sortsPositionsByWhatWasPutIntoThem() {
        portfolio.buy(sap(), Quantity.of(1), euro("100"), euro("0"), JANUARY);
        portfolio.buy(apple(), Quantity.of(10), Money.of("200", "USD"), null, JANUARY);

        assertThat(portfolio.positions()).first()
                .extracting(position -> position.symbol().value()).isEqualTo("AAPL");
    }

    @Test
    void refusesToSellSomethingItDoesNotHold() {
        assertThatExceptionOfType(PositionNotFoundException.class)
                .isThrownBy(() -> portfolio.sell(SAP, Quantity.of(1), euro("100"), null, JUNE));
    }

    @Test
    void keepsAFullySoldPositionAroundForItsRealizedGain() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);
        portfolio.sell(SAP, Quantity.of(10), euro("120"), euro("0"), JUNE);

        assertThat(portfolio.positions()).isEmpty();
        assertThat(portfolio.closedPositions()).hasSize(1);
        assertThat(portfolio.watchedSymbols()).isEmpty();
    }

    @Test
    void valuesEverythingItCanPrice() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);

        PortfolioValuation valuation = portfolio.valuate(Map.of(SAP, euro("130")), ExchangeRates.none());

        assertThat(valuation.invested()).isEqualTo(euro("1000"));
        assertThat(valuation.marketValue()).isEqualTo(euro("1300"));
        assertThat(valuation.unrealizedGain()).isEqualTo(euro("300"));
        assertThat(valuation.unrealizedGainPercent()).isEqualByComparingTo("30");
        assertThat(valuation.isComplete()).isTrue();
    }

    @Test
    void leavesUnpricedPositionsOutOfTheTotalsAndSaysSo() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);
        portfolio.buy(apple(), Quantity.of(1), Money.of("200", "USD"), null, JANUARY);

        PortfolioValuation valuation = portfolio.valuate(Map.of(SAP, euro("130")), ExchangeRates.none());

        assertThat(valuation.marketValue()).isEqualTo(euro("1300"));
        assertThat(valuation.unpricedPositions()).singleElement()
                .satisfies(position -> assertThat(position.instrument().symbol()).isEqualTo(AAPL));
        assertThat(valuation.isComplete()).isFalse();
    }

    @Test
    void convertsForeignPositionsIntoTheBaseCurrencyWhenARateIsAvailable() {
        portfolio.buy(apple(), Quantity.of(10), Money.of("200", "USD"), null, JANUARY);
        ExchangeRates halved = (amount, target) -> target.equals(USD)
                ? Optional.of(amount)
                : Optional.of(Money.of(amount.amount().multiply(java.math.BigDecimal.valueOf(0.5)), target));

        PortfolioValuation valuation = portfolio.valuate(Map.of(AAPL, Money.of("220", "USD")), halved);

        assertThat(valuation.baseCurrency()).isEqualTo(EUR);
        assertThat(valuation.invested()).isEqualTo(euro("1000"));
        assertThat(valuation.marketValue()).isEqualTo(euro("1100"));
        assertThat(valuation.isComplete()).isTrue();
    }

    @Test
    void reportsAForeignPositionAsUnpricedWhenNoRateIsAvailable() {
        portfolio.buy(apple(), Quantity.of(10), Money.of("200", "USD"), null, JANUARY);

        PortfolioValuation valuation = portfolio.valuate(Map.of(AAPL, Money.of("220", "USD")), ExchangeRates.none());

        assertThat(valuation.unpricedPositions()).hasSize(1);
        assertThat(valuation.marketValue().isZero()).isTrue();
    }

    @Test
    void countsRealizedGainsOfClosedPositionsTowardsTheTotal() {
        portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);
        portfolio.sell(SAP, Quantity.of(10), euro("150"), euro("0"), JUNE);

        PortfolioValuation valuation = portfolio.valuate(Map.of(), ExchangeRates.none());

        assertThat(valuation.realizedGain()).isEqualTo(euro("500"));
        assertThat(valuation.totalReturn()).isEqualTo(euro("500"));
    }

    @Test
    void removesAPositionOutright() {
        Position position = portfolio.buy(sap(), Quantity.of(10), euro("100"), euro("0"), JANUARY);

        portfolio.removePosition(position.id());

        assertThat(portfolio.allPositions()).isEmpty();
        assertThatExceptionOfType(PositionNotFoundException.class)
                .isThrownBy(() -> portfolio.removePosition(position.id()));
    }

    @Test
    void insistsOnAName() {
        assertThatIllegalArgumentException().isThrownBy(() -> Portfolio.open("  ", EUR, Instant.now()));
        assertThatIllegalArgumentException().isThrownBy(() -> portfolio.rename(null));
    }

    private static InstrumentRef sap() {
        return new InstrumentRef(SAP, "SAP SE", Isin.of("DE0007164600"), Wkn.of("716460"), EUR);
    }

    private static InstrumentRef apple() {
        return new InstrumentRef(AAPL, "Apple Inc.", Isin.of("US0378331005"), Wkn.of("865985"), USD);
    }

    private static Money euro(String amount) {
        return Money.of(amount, "EUR");
    }
}
