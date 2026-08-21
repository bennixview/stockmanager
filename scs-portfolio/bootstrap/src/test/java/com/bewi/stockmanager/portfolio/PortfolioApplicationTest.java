package com.bewi.stockmanager.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.Quantity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole system, wired as it runs - and with the market data system deliberately unreachable,
 * because that is the interesting case for two systems that are supposed to be independent.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PortfolioApplicationTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private ManagePortfolios portfolios;

    @Autowired
    private TradeInstruments trades;

    @Autowired
    private ViewPortfolio view;

    @Test
    void servesTheDashboardWithoutTheMarketDataSystem() {
        assertThat(mvc.get().uri("/")).hasStatusOk().bodyText().contains("Depot");
    }

    @Test
    void booksATradeAndShowsItAfterwards() {
        Portfolio portfolio = portfolios.createPortfolio("Testdepot", java.util.Currency.getInstance("EUR"));

        trades.buy(new TradeInstruments.BuyOrder(portfolio.id(), "SAP.DE", "SAP SE", new BigDecimal("10"),
                new BigDecimal("100"), BigDecimal.ZERO, "EUR", LocalDate.of(2026, 1, 15)));

        assertThat(view.overview(portfolio.id()).portfolio().positions()).singleElement()
                .satisfies(position -> assertThat(position.quantity()).isEqualTo(Quantity.of(10)));
        assertThat(mvc.get().uri("/portfolios/" + portfolio.id())).hasStatusOk()
                .bodyText().contains("SAP SE");
    }

    @Test
    void reportsPositionsAsUnpricedRatherThanFailingWhenNoPricesAreAvailable() {
        Portfolio portfolio = portfolios.createPortfolio("Ohne Kurse", java.util.Currency.getInstance("EUR"));
        trades.buy(new TradeInstruments.BuyOrder(portfolio.id(), "AAPL", "Apple", new BigDecimal("1"),
                new BigDecimal("200"), BigDecimal.ZERO, "EUR", LocalDate.of(2026, 1, 15)));

        ViewPortfolio.Overview overview = view.overview(portfolio.id());

        assertThat(overview.valuation().isComplete()).isFalse();
        assertThat(overview.valuation().unpricedPositions()).hasSize(1);
    }

    @Test
    void servesItsOwnApi() {
        assertThat(mvc.get().uri("/api/portfolios")).hasStatusOk();
    }

    @Test
    void answersTheHealthProbe() {
        assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
    }
}
