package com.bewi.stockmanager.marketdata.domain;

/** Raised when a symbol is asked for that this system does not know. */
public class UnknownInstrumentException extends RuntimeException {

    private final Symbol symbol;

    public UnknownInstrumentException(Symbol symbol) {
        super("No instrument known for symbol " + symbol);
        this.symbol = symbol;
    }

    public Symbol symbol() {
        return symbol;
    }
}
