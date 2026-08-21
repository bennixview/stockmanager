package com.bewi.stockmanager.portfolio.domain;

/** Raised when a position is addressed that the portfolio does not hold. */
public class PositionNotFoundException extends RuntimeException {

    public PositionNotFoundException(PositionId positionId) {
        super("No position " + positionId + " in this portfolio");
    }

    public PositionNotFoundException(Symbol symbol) {
        super("No position in " + symbol + " in this portfolio");
    }
}
