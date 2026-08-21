package com.bewi.stockmanager.marketdata.application.port.in;

import com.bewi.stockmanager.marketdata.domain.Quote;

/** Callback a subscriber hands in to receive quotes as they arrive. */
@FunctionalInterface
public interface QuoteListener {

    void onQuote(Quote quote);
}
