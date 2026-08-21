package com.bewi.stockmanager.portfolio.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bewi.stockmanager.portfolio.application.fixture.InMemoryPortfolioRepository;
import com.bewi.stockmanager.portfolio.application.fixture.StubMarketData;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments.BuyOrder;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioQueryServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final Symbol SAP = Symbol.of("SAP.DE");

    private InMemoryPortfolioRepository portfolios;
    private StubMarketData marketData;
    private PortfolioService writes;
    private PortfolioQueryService reads;

    @BeforeEach
    void setUp() {
        portfolios = new InMemoryPortfolioRepository();
        marketData = new StubMarketData().knows(StubMarketData.instrument("SAP.DE", "SAP SE", "EUR"));
        writes = new PortfolioService(portfolios, marketData, EUR, CLOCK);
        reads = new PortfolioQueryService(portfolios, marketData, ExchangeRates.none(), writes);
    }

    @Test
    void valuesThePortfolioAtCurrentPrices() {
        Portfolio portfolio = givenAPortfolioHolding("SAP.DE", "10", "100");
        marketData.prices(SAP, "130", "EUR");

        ViewPortfolio.Overview overview = reads.overview(portfolio.id());

        assertThat(overview.valuation().marketValue()).isEqualTo(Money.of("1300", "EUR"));
        assertThat(overview.prices()).containsKey(SAP);
    }

    @Test
    void stillRendersThePortfolioWhenTheMarketDataSystemIsDown() {
        Portfolio portfolio = givenAPortfolioHolding("SAP.DE", "10", "100");
        PortfolioQueryService offline = new PortfolioQueryService(portfolios, new StubMarketData().unavailable(),
                ExchangeRates.none(), writes);

        ViewPortfolio.Overview overview = offline.overview(portfolio.id());

        assertThat(overview.valuation().unpricedPositions()).hasSize(1);
        assertThat(overview.valuation().invested()).isEqualTo(Money.of("0", "EUR"));
        assertThat(overview.portfolio().positions()).hasSize(1);
    }

    @Test
    void createsTheDefaultPortfolioWhenThereIsNoneYet() {
        ViewPortfolio.Overview overview = reads.defaultOverview();

        assertThat(overview.portfolio().name()).isNotBlank();
        assertThat(portfolios.count()).isEqualTo(1);
    }

    @Test
    void servesOnePositionWithItsValuation() {
        Portfolio portfolio = givenAPortfolioHolding("SAP.DE", "10", "100");
        marketData.prices(SAP, "130", "EUR");
        var positionId = portfolios.findById(portfolio.id()).orElseThrow().positions().getFirst().id();

        ViewPortfolio.PositionDetails details = reads.position(portfolio.id(), positionId);

        assertThat(details.valuation().marketValue()).isEqualTo(Money.of("1300", "EUR"));
        assertThat(details.price().price()).isEqualTo(Money.of("130", "EUR"));
        assertThat(details.position().tranches()).hasSize(1);
    }

    private Portfolio givenAPortfolioHolding(String symbol, String quantity, String price) {
        Portfolio portfolio = writes.createPortfolio("Depot", EUR);
        writes.buy(new BuyOrder(portfolio.id(), symbol, symbol, new BigDecimal(quantity), new BigDecimal(price),
                BigDecimal.ZERO, "EUR", LocalDate.of(2026, 1, 15)));
        return portfolio;
    }
}
