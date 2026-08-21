package com.bewi.stockmanager.portfolio.adapter.out.marketdata;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The contract with the other self-contained system, including how this one behaves when that
 * system is unavailable.
 */
class MarketDataRestAdapterTest {

    private static final String BASE = "http://marketdata:8081";

    private MockRestServiceServer server;
    private MarketDataPort adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new MarketDataRestAdapter(builder.build());
    }

    @Test
    void readsAQuoteIntoTheLocalModel() {
        server.expect(requestTo(BASE + "/api/quotes/AAPL")).andRespond(withSuccess("""
                {"symbol":"AAPL","price":226.05,"currency":"USD","previousClose":220.00,"change":6.05,
                 "changePercent":2.75,"marketState":"OPEN","asOf":"2026-05-04T12:00:00Z"}
                """, MediaType.APPLICATION_JSON));

        assertThat(adapter.priceFor(Symbol.of("AAPL"))).hasValueSatisfying(price -> {
            assertThat(price.price()).isEqualTo(Money.of("226.05", "USD"));
            assertThat(price.changePercent()).isEqualByComparingTo("2.75");
        });
    }

    @Test
    // The comma travels percent-encoded; Spring MVC decodes it and splits the list on the other side.
    void asksForSeveralSymbolsInOneRequest() {
        server.expect(requestTo(BASE + "/api/quotes?symbols=AAPL%2CSAP.DE")).andRespond(withSuccess("""
                [{"symbol":"AAPL","price":226.05,"currency":"USD","asOf":"2026-05-04T12:00:00Z"},
                 {"symbol":"SAP.DE","price":190.00,"currency":"EUR","asOf":"2026-05-04T12:00:00Z"}]
                """, MediaType.APPLICATION_JSON));

        assertThat(adapter.pricesFor(List.of(Symbol.of("AAPL"), Symbol.of("SAP.DE"))))
                .containsOnlyKeys(Symbol.of("AAPL"), Symbol.of("SAP.DE"));
    }

    @Test
    void treatsAnUnknownInstrumentAsAnAnswerRatherThanAFailure() {
        server.expect(requestTo(BASE + "/api/instruments/NOSUCH")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.bySymbol(Symbol.of("NOSUCH"))).isEmpty();
    }

    @Test
    void degradesToNoPricesWhenTheOtherSystemFails() {
        server.expect(requestTo(BASE + "/api/quotes?symbols=AAPL")).andRespond(withServerError());

        assertThat(adapter.pricesFor(List.of(Symbol.of("AAPL")))).isEmpty();
    }

    @Test
    void degradesToNoResultsWhenSearchIsUnavailable() {
        server.expect(requestTo(BASE + "/api/instruments?query=apple&limit=5")).andRespond(withServerError());

        assertThat(adapter.search("apple", 5)).isEmpty();
    }

    @Test
    void resolvesAWknForTheLegacyImport() {
        server.expect(requestTo(BASE + "/api/instruments/by-wkn/906866")).andRespond(withSuccess("""
                {"symbol":"AMZN","name":"Amazon.com Inc.","isin":"US0231351067","wkn":"906866",
                 "currency":"USD","exchange":"NASDAQ","type":"STOCK"}
                """, MediaType.APPLICATION_JSON));

        assertThat(adapter.byWkn(Wkn.of("906866"))).hasValueSatisfying(instrument -> {
            assertThat(instrument.symbol()).isEqualTo(Symbol.of("AMZN"));
            assertThat(instrument.currency().getCurrencyCode()).isEqualTo("USD");
        });
    }

    @Test
    void asksForNothingWhenThereIsNothingToPrice() {
        assertThat(adapter.pricesFor(List.of())).isEmpty();
        server.verify();
    }
}
