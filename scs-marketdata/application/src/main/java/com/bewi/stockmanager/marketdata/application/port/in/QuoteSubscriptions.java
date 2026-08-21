package com.bewi.stockmanager.marketdata.application.port.in;

import java.util.Set;

import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Driving port for live data: a client registers interest in a set of symbols and is called back
 * whenever a new price arrives for one of them.
 */
public interface QuoteSubscriptions {

    Subscription subscribe(Set<Symbol> symbols, QuoteListener listener);

    /** Symbols at least one subscriber is currently watching. */
    Set<Symbol> watchedSymbols();

    /** A live subscription; closing it stops the callbacks and releases the symbols. */
    interface Subscription extends AutoCloseable {

        Set<Symbol> symbols();

        @Override
        void close();
    }
}
