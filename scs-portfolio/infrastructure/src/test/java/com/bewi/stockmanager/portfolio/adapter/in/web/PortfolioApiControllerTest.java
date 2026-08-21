package com.bewi.stockmanager.portfolio.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.InsufficientSharesException;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

/** The portfolio system's HTTP contract. */
@WebMvcTest(controllers = PortfolioApiController.class)
@Import(PortfolioExceptionHandler.class)
class PortfolioApiControllerTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Symbol SAP = Symbol.of("SAP.DE");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ViewPortfolio viewPortfolio;

    @MockitoBean
    private ManagePortfolios managePortfolios;

    @MockitoBean
    private TradeInstruments trades;

    @Test
    void servesAPortfolioWithItsValuation() {
        Portfolio portfolio = portfolioHoldingSap();
        given(viewPortfolio.overview(portfolio.id())).willReturn(overviewOf(portfolio));

        var body = assertThat(mvc.get().uri("/api/portfolios/" + portfolio.id())).hasStatusOk().bodyJson();

        body.extractingPath("$.marketValue").asNumber().isEqualTo(1300.0);
        body.extractingPath("$.positions[0].symbol").isEqualTo("SAP.DE");
    }

    @Test
    void booksABuy() {
        Portfolio portfolio = portfolioHoldingSap();
        given(viewPortfolio.overview(any())).willReturn(overviewOf(portfolio));

        assertThat(mvc.post().uri("/api/portfolios/" + portfolio.id() + "/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"symbol":"SAP.DE","quantity":10,"pricePerShare":100,"fee":0,"currency":"EUR",
                         "tradeDate":"2026-01-15"}
                        """))
                .hasStatus(201);
    }

    @Test
    void rejectsATradeWithoutAQuantity() {
        assertThat(mvc.post().uri("/api/portfolios/" + PortfolioId.generate() + "/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"symbol":"SAP.DE","pricePerShare":100}
                        """))
                .hasStatus(400);
    }

    @Test
    void reportsAnOversizedSaleAsAConflictWithTheNumbers() {
        PortfolioId id = PortfolioId.generate();
        willThrow(new InsufficientSharesException(SAP, Quantity.of(5), Quantity.of(10)))
                .given(trades).sell(any());

        assertThat(mvc.post().uri("/api/portfolios/" + id + "/sell")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"symbol":"SAP.DE","quantity":10,"pricePerShare":100}
                        """))
                .hasStatus(409)
                .bodyJson()
                .extractingPath("$.symbol").isEqualTo("SAP.DE");
    }

    @Test
    void listsPortfolios() {
        given(managePortfolios.listPortfolios()).willReturn(List.of(portfolioHoldingSap()));

        assertThat(mvc.get().uri("/api/portfolios")).hasStatusOk()
                .bodyJson().extractingPath("$[0].name").isEqualTo("Mein Depot");
    }

    private static Portfolio portfolioHoldingSap() {
        Portfolio portfolio = Portfolio.restore(PortfolioId.generate(), "Mein Depot", EUR,
                Instant.parse("2026-01-01T00:00:00Z"), List.of());
        portfolio.buy(InstrumentRef.of(SAP, "SAP SE", EUR), Quantity.of(10), Money.of("100", "EUR"), null,
                LocalDate.of(2026, 1, 15));
        return portfolio;
    }

    private static ViewPortfolio.Overview overviewOf(Portfolio portfolio) {
        return new ViewPortfolio.Overview(portfolio,
                portfolio.valuate(Map.of(SAP, Money.of("130", "EUR")), ExchangeRates.none()),
                Map.of(), List.of(portfolio));
    }
}
