package com.bewi.stockmanager.marketdata.adapter.in.web;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.bewi.stockmanager.marketdata.application.port.in.InstrumentCatalog;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteQuery;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteSubscriptions;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/** The HTTP contract other systems depend on. */
@WebMvcTest(controllers = {QuoteApiController.class, InstrumentApiController.class})
@Import(MarketDataExceptionHandler.class)
class QuoteApiControllerTest {

    private static final Instant NOON = Instant.parse("2026-05-04T12:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private QuoteQuery quotes;

    @MockitoBean
    private InstrumentCatalog catalog;

    @MockitoBean
    private QuoteSubscriptions subscriptions;

    @Test
    void servesOneQuoteWithItsDerivedChange() {
        given(quotes.quoteFor(Symbol.of("AAPL")))
                .willReturn(Optional.of(Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), Money.of("100", "USD"),
                        NOON)));

        assertThat(mvc.get().uri("/api/quotes/AAPL")).hasStatusOk()
                .bodyJson()
                .extractingPath("$.price").isEqualTo(110.0);
    }

    @Test
    void answersNotFoundWhenNothingCanPriceTheSymbol() {
        given(quotes.quoteFor(any())).willReturn(Optional.empty());

        assertThat(mvc.get().uri("/api/quotes/NOSUCH")).hasStatus(404);
    }

    @Test
    void servesSeveralQuotesInOneCallSortedBySymbol() {
        given(quotes.quotesFor(any())).willReturn(Map.of(
                Symbol.of("MSFT"), Quote.of(Symbol.of("MSFT"), Money.of("410", "USD"), null, NOON),
                Symbol.of("AAPL"), Quote.of(Symbol.of("AAPL"), Money.of("110", "USD"), null, NOON)));

        assertThat(mvc.get().uri("/api/quotes?symbols=AAPL,MSFT")).hasStatusOk()
                .bodyJson()
                .extractingPath("$[*].symbol").asArray().containsExactly("AAPL", "MSFT");
    }

    @Test
    void rejectsAnUnusableSymbolWithABadRequestRatherThanAServerError() {
        assertThat(mvc.get().uri("/api/quotes/not a symbol")).hasStatus4xxClientError();
    }
}
