package com.bewi.stockmanager.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The portfolio self-contained system.
 *
 * <p>Owns portfolios, positions and trades, with its own database and its own UI. It knows nothing
 * about where prices come from beyond one HTTP contract with the market data system.
 */
@SpringBootApplication
public class PortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}
