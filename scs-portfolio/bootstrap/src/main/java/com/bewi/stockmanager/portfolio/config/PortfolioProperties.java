package com.bewi.stockmanager.portfolio.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * What this system needs to be told about its surroundings.
 *
 * @param baseCurrency     the currency new portfolios are kept in
 * @param exchangeRateTtl  how long a fetched FX rate is reused
 */
@ConfigurationProperties("portfolio")
public record PortfolioProperties(
        @DefaultValue("EUR") String baseCurrency,
        @DefaultValue("5m") Duration exchangeRateTtl,
        @DefaultValue MarketData marketData,
        @DefaultValue LegacyImport legacyImport) {

    /**
     * The market data system.
     *
     * @param baseUrl    where this application reaches it, server to server
     * @param browserUrl where the user's browser reaches it, for the quote widget and the event
     *                   stream; differs from {@code baseUrl} whenever the systems run in containers
     */
    public record MarketData(
            @DefaultValue("http://localhost:8081") String baseUrl,
            @DefaultValue("http://localhost:8081") String browserUrl,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("5s") Duration readTimeout) {
    }

    /**
     * One-off import of the {@code positions.json} of the previous version.
     *
     * @param enabled whether to look for the file at all
     * @param file    where it is; the import only runs when no portfolio exists yet
     */
    public record LegacyImport(@DefaultValue("true") boolean enabled, @DefaultValue("positions.json") String file) {
    }
}
