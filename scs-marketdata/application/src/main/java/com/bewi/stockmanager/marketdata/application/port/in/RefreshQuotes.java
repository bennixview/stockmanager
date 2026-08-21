package com.bewi.stockmanager.marketdata.application.port.in;

/**
 * Driving port for whatever drives the refresh cycle - a scheduler in production, a test calling it
 * directly. Keeping the trigger outside the application layer keeps scheduling out of the domain.
 */
public interface RefreshQuotes {

    /** Pulls fresh prices for every watched symbol and notifies subscribers about changes. */
    int refreshWatchedQuotes();
}
