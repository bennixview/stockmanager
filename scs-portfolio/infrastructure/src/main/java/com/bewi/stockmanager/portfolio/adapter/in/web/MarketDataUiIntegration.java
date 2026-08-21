package com.bewi.stockmanager.portfolio.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Puts the market data system's public URL on every page.
 *
 * <p>This is the UI half of the integration between the two self-contained systems: the pages load
 * the quote widget from that origin, so live prices are rendered by the system that owns them and
 * this one never learns how a price is formatted or where it comes from. The link is configuration,
 * not a compile-time dependency.
 */
@ControllerAdvice
class MarketDataUiIntegration {

    private final String marketDataUrl;

    MarketDataUiIntegration(
            @Value("${portfolio.market-data.browser-url:http://localhost:8081}") String marketDataUrl) {
        this.marketDataUrl = marketDataUrl;
    }

    @ModelAttribute("marketDataUrl")
    String marketDataUrl() {
        return marketDataUrl;
    }
}
