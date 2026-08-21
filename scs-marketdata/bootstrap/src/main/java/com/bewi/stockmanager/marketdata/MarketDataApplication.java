package com.bewi.stockmanager.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The market data self-contained system.
 *
 * <p>Owns instruments and prices, has its own database, its own UI and its own release cycle. The
 * portfolio system talks to it over HTTP and embeds its quote widget - never its database.
 */
@SpringBootApplication
@EnableScheduling
public class MarketDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataApplication.class, args);
    }
}
