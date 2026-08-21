package com.bewi.stockmanager.portfolio.domain;

/** Raised when a portfolio is addressed that does not exist. */
public class PortfolioNotFoundException extends RuntimeException {

    public PortfolioNotFoundException(PortfolioId portfolioId) {
        super("No portfolio " + portfolioId);
    }
}
