package com.bewi.stockmanager.marketdata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bewi.stockmanager.marketdata.application.port.in.RefreshQuotes;

/**
 * Drives the live data cycle.
 *
 * <p>Scheduling is a runtime concern, so it sits here and only pulls the trigger the application
 * layer exposes. Nothing is polled while no browser is watching.
 */
@Component
class QuoteRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuoteRefreshScheduler.class);

    private final RefreshQuotes refreshQuotes;

    QuoteRefreshScheduler(RefreshQuotes refreshQuotes) {
        this.refreshQuotes = refreshQuotes;
    }

    @Scheduled(fixedDelayString = "${marketdata.refresh-interval:15s}", initialDelayString = "${marketdata.refresh-interval:15s}")
    void refresh() {
        try {
            int updated = refreshQuotes.refreshWatchedQuotes();
            if (updated > 0) {
                log.debug("Pushed {} updated quotes to subscribers", updated);
            }
        } catch (RuntimeException e) {
            log.error("Quote refresh cycle failed", e);
        }
    }
}
