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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.SearchInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

/** The web adapter: what the pages show and what they hand to the use cases. */
@WebMvcTest(controllers = {PortfolioWebController.class, TradeWebController.class})
@Import(WebErrorController.class)
class PortfolioWebControllerTest {

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

    @MockitoBean
    private SearchInstruments searchInstruments;

    @Test
    void rendersTheDashboardWithItsPositions() {
        given(viewPortfolio.defaultOverview()).willReturn(overview());

        assertThat(mvc.get().uri("/")).hasStatusOk()
                .bodyText().contains("SAP SE").contains("Mein Depot");
    }

    @Test
    void embedsTheMarketDataSystemsQuoteWidget() {
        given(viewPortfolio.defaultOverview()).willReturn(overview());

        assertThat(mvc.get().uri("/")).hasStatusOk()
                .bodyText()
                .contains("<market-quote")
                .contains("/js/market-quote.js");
    }

    @Test
    void showsAPageInsteadOfAStackTraceForAPortfolioThatIsGone() {
        PortfolioId missing = PortfolioId.generate();
        willThrow(new PortfolioNotFoundException(missing)).given(viewPortfolio).overview(missing);

        assertThat(mvc.get().uri("/portfolios/" + missing)).hasStatus(404)
                .bodyText().contains("Nicht gefunden");
    }

    @Test
    void booksABuyAndRedirectsToThePosition() {
        Portfolio portfolio = portfolio();
        var position = portfolio.buy(instrument(), Quantity.of(1), Money.of("100", "EUR"), null,
                LocalDate.of(2026, 1, 15));
        given(trades.buy(any())).willReturn(position);

        assertThat(mvc.post().uri("/portfolios/" + portfolio.id() + "/buy")
                .param("symbol", "SAP.DE")
                .param("name", "SAP SE")
                .param("quantity", "10")
                .param("pricePerShare", "100")
                .param("fee", "0")
                .param("currency", "EUR")
                .param("tradeDate", "2026-01-15"))
                .hasStatus3xxRedirection();

        then(trades).should().buy(any(TradeInstruments.BuyOrder.class));
    }

    @Test
    void redisplaysTheBuyFormWhenItIsIncomplete() {
        assertThat(mvc.post().uri("/portfolios/" + PortfolioId.generate() + "/buy")
                .param("symbol", "")
                .param("quantity", "10")
                .param("pricePerShare", "100")
                .param("tradeDate", "2026-01-15"))
                .hasStatusOk()
                .bodyText().contains("Pick an instrument");

        then(trades).shouldHaveNoInteractions();
    }

    @Test
    void refusesATradeDatedInTheFuture() {
        assertThat(mvc.post().uri("/portfolios/" + PortfolioId.generate() + "/buy")
                .param("symbol", "SAP.DE")
                .param("quantity", "10")
                .param("pricePerShare", "100")
                .param("tradeDate", LocalDate.now().plusDays(1).toString()))
                .hasStatusOk()
                .bodyText().contains("cannot be booked for the future");

        then(trades).shouldHaveNoInteractions();
    }

    @Test
    void servesInstrumentSearchAsAFragment() {
        given(searchInstruments.search("sap")).willReturn(List.of(instrument()));

        assertThat(mvc.get().uri("/instruments/search?query=sap")).hasStatusOk()
                .bodyText().contains("SAP SE").doesNotContain("<html");
    }

    private ViewPortfolio.Overview overview() {
        Portfolio portfolio = portfolio();
        portfolio.buy(instrument(), Quantity.of(10), Money.of("100", "EUR"), null, LocalDate.of(2026, 1, 15));
        return new ViewPortfolio.Overview(portfolio,
                portfolio.valuate(Map.of(SAP, Money.of("130", "EUR")), ExchangeRates.none()),
                Map.of(), List.of(portfolio));
    }

    private static Portfolio portfolio() {
        return Portfolio.restore(PortfolioId.generate(), "Mein Depot", EUR, Instant.parse("2026-01-01T00:00:00Z"),
                List.of());
    }

    private static InstrumentRef instrument() {
        return InstrumentRef.of(SAP, "SAP SE", EUR);
    }
}
