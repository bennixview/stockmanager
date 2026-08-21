package com.bewi.stockmanager.marketdata.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Everything this system needs to be told about its surroundings.
 *
 * @param provider     which quote feed to use: {@code simulated}, {@code yahoo} or {@code alphavantage}
 * @param quoteMaxAge  how old a cached quote may get before a read refreshes it
 * @param refreshInterval how often watched symbols are polled for live updates
 */
@ConfigurationProperties("marketdata")
public record MarketDataProperties(
        @DefaultValue("simulated") String provider,
        @DefaultValue("30s") Duration quoteMaxAge,
        @DefaultValue("15s") Duration refreshInterval,
        @DefaultValue Cache cache,
        @DefaultValue Yahoo yahoo,
        @DefaultValue AlphaVantage alphaVantage,
        @DefaultValue("http://localhost:8080") List<String> allowedOrigins) {

    public record Cache(@DefaultValue("5000") int maximumSize, @DefaultValue("10m") Duration timeToLive) {
    }

    public record Yahoo(
            @DefaultValue("https://query1.finance.yahoo.com") String baseUrl,
            @DefaultValue("3s") Duration connectTimeout,
            @DefaultValue("8s") Duration readTimeout) {
    }

    public record AlphaVantage(
            @DefaultValue("https://www.alphavantage.co") String baseUrl,
            @DefaultValue("") String apiKey,
            @DefaultValue("3s") Duration connectTimeout,
            @DefaultValue("8s") Duration readTimeout) {
    }
}
