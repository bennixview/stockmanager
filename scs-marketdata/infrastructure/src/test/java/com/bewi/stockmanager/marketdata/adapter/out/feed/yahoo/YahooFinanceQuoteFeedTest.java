package com.bewi.stockmanager.marketdata.adapter.out.feed.yahoo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.MarketState;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Pins the parsing of Yahoo's chart response, including the shapes that trip a naive parser. */
class YahooFinanceQuoteFeedTest {

    private static final String CHART_RESPONSE = """
            {"chart":{"result":[{
              "meta":{"currency":"USD","symbol":"AAPL","exchangeName":"NMS","regularMarketPrice":226.05,
                      "chartPreviousClose":220.00,"regularMarketTime":1777982400,"marketState":"REGULAR"},
              "timestamp":[1777896000,1777982400,1778068800],
              "indicators":{"quote":[{"open":[219.0,221.0,225.0],"high":[222.0,226.0,227.0],
                                      "low":[218.0,220.5,224.0],"close":[221.0,null,226.05],
                                      "volume":[1000,2000,3000]}]}
            }],"error":null}}
            """;

    private final Instrument apple = Instrument.builder(Symbol.of("AAPL")).name("Apple").currency("USD").build();

    private RestClient restClient;
    private MockRestServiceServer server;
    private YahooFinanceQuoteFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://query1.finance.yahoo.com");
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        feed = new YahooFinanceQuoteFeed(restClient);
    }

    @Test
    void readsPriceCurrencyAndPreviousCloseFromTheMeta() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1d&interval=5m"))
                .andRespond(withSuccess(CHART_RESPONSE, MediaType.APPLICATION_JSON));

        Quote quote = feed.latestQuote(apple).orElseThrow();

        assertThat(quote.price()).isEqualTo(Money.of("226.05", "USD"));
        assertThat(quote.previousClose()).isEqualTo(Money.of("220.00", "USD"));
        assertThat(quote.marketState()).isEqualTo(MarketState.OPEN);
        assertThat(quote.changePercent()).isEqualByComparingTo("2.7500");
    }

    @Test
    void skipsTheNullsYahooPadsGapsWith() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1mo&interval=1d"))
                .andRespond(withSuccess(CHART_RESPONSE, MediaType.APPLICATION_JSON));

        PriceHistory history = feed.history(apple, HistoryRange.MONTH);

        assertThat(history.candles()).hasSize(2);
        assertThat(history.candles()).extracting(candle -> candle.close().toPlainString())
                .containsExactly("221.0", "226.05");
    }

    @Test
    void answersEmptyRatherThanThrowingWhenYahooIsUnhappy() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1d&interval=5m"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThat(feed.latestQuote(apple)).isEmpty();
    }

    @Test
    void answersEmptyWhenTheSymbolIsUnknown() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1d&interval=5m"))
                .andRespond(withSuccess("""
                        {"chart":{"result":null,"error":{"code":"Not Found","description":"No data found"}}}
                        """, MediaType.APPLICATION_JSON));

        assertThat(feed.latestQuote(apple)).isEmpty();
    }

    @Test
    void convertsLondonPenceIntoPounds() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1d&interval=5m"))
                .andRespond(withSuccess("""
                        {"chart":{"result":[{"meta":{"currency":"GBp","regularMarketPrice":250.0}}]}}
                        """, MediaType.APPLICATION_JSON));

        assertThat(feed.latestQuote(apple).orElseThrow().price()).isEqualTo(Money.of("2.50", "GBP"));
    }

    @Test
    void keepsTheInstrumentsCurrencyWhenYahooReportsSomethingUnmappable() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1d&interval=5m"))
                .andRespond(withSuccess("""
                        {"chart":{"result":[{"meta":{"currency":"XYZ","regularMarketPrice":100.0}}]}}
                        """, MediaType.APPLICATION_JSON));

        assertThat(feed.latestQuote(apple).orElseThrow().price().currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void returnsAnEmptyHistoryRatherThanFailingWhenTheChartHasNoCandles() {
        server.expect(requestTo("https://query1.finance.yahoo.com/v8/finance/chart/AAPL?range=1y&interval=1d"))
                .andRespond(withSuccess("""
                        {"chart":{"result":[{"meta":{"currency":"USD","regularMarketPrice":1.0}}]}}
                        """, MediaType.APPLICATION_JSON));

        assertThat(feed.history(apple, HistoryRange.YEAR).isEmpty()).isTrue();
    }
}
