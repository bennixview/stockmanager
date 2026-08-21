package com.bewi.stockmanager.marketdata.config;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.marketdata.adapter.out.cache.CaffeineQuoteCache;
import com.bewi.stockmanager.marketdata.adapter.out.feed.MarketDataFeed;
import com.bewi.stockmanager.marketdata.adapter.out.feed.alphavantage.AlphaVantageQuoteFeed;
import com.bewi.stockmanager.marketdata.adapter.out.feed.simulated.SimulatedQuoteFeed;
import com.bewi.stockmanager.marketdata.adapter.out.feed.yahoo.YahooFinanceQuoteFeed;
import com.bewi.stockmanager.marketdata.application.port.out.QuoteCache;
import com.bewi.stockmanager.marketdata.application.service.InstrumentCatalogService;
import com.bewi.stockmanager.marketdata.application.service.QuoteService;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;

/**
 * Wires the hexagon.
 *
 * <p>The domain and application modules carry no framework annotations, so this is the one place
 * that knows both what the use cases need and which adapter supplies it.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MarketDataProperties.class)
public class MarketDataConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MarketDataConfiguration.class);

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    QuoteCache quoteCache(MarketDataProperties properties) {
        return new CaffeineQuoteCache(properties.cache().maximumSize(), properties.cache().timeToLive());
    }

    @Bean
    MarketDataFeed marketDataFeed(MarketDataProperties properties, RestClient.Builder restClientBuilder, Clock clock) {
        String provider = properties.provider().toLowerCase(Locale.ROOT);
        log.info("Using '{}' as quote provider", provider);
        return switch (provider) {
            case "yahoo" -> new YahooFinanceQuoteFeed(yahooRestClient(properties, restClientBuilder));
            case "alphavantage" -> new AlphaVantageQuoteFeed(alphaVantageRestClient(properties, restClientBuilder),
                    properties.alphaVantage().apiKey());
            case "simulated" -> new SimulatedQuoteFeed(clock);
            default -> throw new IllegalArgumentException(
                    "Unknown marketdata.provider '%s'; expected simulated, yahoo or alphavantage".formatted(provider));
        };
    }

    @Bean
    QuoteService quoteService(InstrumentRepository instruments, MarketDataFeed feed, QuoteCache cache,
            MarketDataProperties properties, Clock clock) {
        return new QuoteService(instruments, feed, feed, cache, properties.quoteMaxAge(), clock);
    }

    @Bean
    InstrumentCatalogService instrumentCatalogService(InstrumentRepository instruments) {
        return new InstrumentCatalogService(instruments);
    }

    private static RestClient yahooRestClient(MarketDataProperties properties, RestClient.Builder builder) {
        MarketDataProperties.Yahoo yahoo = properties.yahoo();
        return builder.clone()
                .baseUrl(yahoo.baseUrl())
                // Yahoo's public endpoints answer with 429 to clients that do not identify themselves.
                .defaultHeader(HttpHeaders.USER_AGENT, "stockmanager/1.0 (+https://github.com/bennixview/stockmanager)")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .requestFactory(requestFactory(yahoo.connectTimeout(), yahoo.readTimeout()))
                .build();
    }

    private static RestClient alphaVantageRestClient(MarketDataProperties properties, RestClient.Builder builder) {
        MarketDataProperties.AlphaVantage alphaVantage = properties.alphaVantage();
        return builder.clone()
                .baseUrl(alphaVantage.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .requestFactory(requestFactory(alphaVantage.connectTimeout(), alphaVantage.readTimeout()))
                .build();
    }

    /**
     * A request factory with timeouts.
     *
     * <p>A quote feed that hangs must not hang the page that asked for a price, so both the connect
     * and the read timeout are short and configurable.
     */
    private static ClientHttpRequestFactory requestFactory(Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }
}
