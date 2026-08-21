package com.bewi.stockmanager.portfolio.domain;

/** Raised when a sale would take more shares out of a position than it holds. */
public class InsufficientSharesException extends RuntimeException {

    private final Symbol symbol;
    private final Quantity held;
    private final Quantity requested;

    public InsufficientSharesException(Symbol symbol, Quantity held, Quantity requested) {
        super("Cannot sell %s shares of %s, only %s held".formatted(requested, symbol, held));
        this.symbol = symbol;
        this.held = held;
        this.requested = requested;
    }

    public Symbol symbol() {
        return symbol;
    }

    public Quantity held() {
        return held;
    }

    public Quantity requested() {
        return requested;
    }
}
