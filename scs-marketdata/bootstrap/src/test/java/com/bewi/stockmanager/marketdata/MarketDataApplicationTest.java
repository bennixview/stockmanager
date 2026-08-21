package com.bewi.stockmanager.marketdata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.bewi.stockmanager.marketdata.application.port.in.InstrumentCatalog;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteQuery;
import com.bewi.stockmanager.marketdata.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

/** The whole system, wired as it runs: Flyway, the catalogue, the feed and the web layer. */
@SpringBootTest
@AutoConfigureMockMvc
class MarketDataApplicationTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private InstrumentCatalog catalog;

    @Autowired
    private QuoteQuery quotes;

    @Test
    void migratesAndSeedsTheCatalogueOnStartup() {
        assertThat(catalog.bySymbol(Symbol.of("AAPL"))).isPresent();
        assertThat(catalog.all()).hasSizeGreaterThan(20);
    }

    @Test
    void pricesASeededInstrumentThroughTheApi() {
        assertThat(mvc.get().uri("/api/quotes/AAPL")).hasStatusOk()
                .bodyJson().extractingPath("$.currency").isEqualTo("USD");
    }

    @Test
    void servesTheInstrumentSearchPage() {
        assertThat(mvc.get().uri("/?query=apple")).hasStatusOk()
                .bodyText().contains("Apple Inc.").contains("<market-quote");
    }

    @Test
    void offersItsQuoteWidgetToTheOtherSystem() {
        assertThat(mvc.get().uri("/js/market-quote.js")).hasStatusOk()
                .bodyText().contains("customElements.define('market-quote'");
    }

    @Test
    void answersTheHealthProbe() {
        assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
    }

    @Test
    void pricesAnythingTheFeedKnowsEvenWithoutACatalogueEntry() {
        assertThat(quotes.quoteFor(Symbol.of("SOMETHING"))).isPresent();
    }
}
