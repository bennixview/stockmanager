package com.bewi.stockmanager.portfolio.config;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Currency;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.portfolio.adapter.in.legacy.LegacyPositionsImporter;
import com.bewi.stockmanager.portfolio.adapter.out.marketdata.MarketDataExchangeRates;
import com.bewi.stockmanager.portfolio.adapter.out.marketdata.MarketDataRestAdapter;
import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.application.service.InstrumentSearchService;
import com.bewi.stockmanager.portfolio.application.service.PortfolioQueryService;
import com.bewi.stockmanager.portfolio.application.service.PortfolioService;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;

/**
 * Wires the hexagon.
 *
 * <p>Domain and application carry no framework annotations; this is where they meet their adapters.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PortfolioProperties.class)
public class PortfolioConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    MarketDataPort marketDataPort(PortfolioProperties properties, RestClient.Builder builder) {
        PortfolioProperties.MarketData marketData = properties.marketData();
        RestClient restClient = builder.clone()
                .baseUrl(marketData.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .requestFactory(requestFactory(marketData.connectTimeout(), marketData.readTimeout()))
                .build();
        return new MarketDataRestAdapter(restClient);
    }

    @Bean
    ExchangeRates exchangeRates(MarketDataPort marketData, PortfolioProperties properties, Clock clock) {
        return new MarketDataExchangeRates(marketData, properties.exchangeRateTtl(), clock);
    }

    @Bean
    PortfolioService portfolioService(PortfolioRepository portfolios, MarketDataPort marketData,
            PortfolioProperties properties, Clock clock) {
        return new PortfolioService(portfolios, marketData, Currency.getInstance(properties.baseCurrency()), clock);
    }

    @Bean
    PortfolioQueryService portfolioQueryService(PortfolioRepository portfolios, MarketDataPort marketData,
            ExchangeRates exchangeRates, ManagePortfolios managePortfolios) {
        return new PortfolioQueryService(portfolios, marketData, exchangeRates, managePortfolios);
    }

    @Bean
    InstrumentSearchService instrumentSearchService(MarketDataPort marketData) {
        return new InstrumentSearchService(marketData);
    }

    @Bean
    LegacyPositionsImporter legacyPositionsImporter(TradeInstruments trades, ManagePortfolios portfolios,
            MarketDataPort marketData, Clock clock) {
        return new LegacyPositionsImporter(trades, portfolios, marketData, clock.getZone());
    }

    /**
     * A request factory with timeouts.
     *
     * <p>The market data system being slow must not make this system slow: a page renders without
     * prices rather than waiting for them.
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
