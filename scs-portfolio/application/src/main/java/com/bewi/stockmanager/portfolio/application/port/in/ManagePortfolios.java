package com.bewi.stockmanager.portfolio.application.port.in;

import java.util.Currency;
import java.util.List;

import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;

/** Driving port for the portfolios themselves, as opposed to what is in them. */
public interface ManagePortfolios {

    Portfolio createPortfolio(String name, Currency baseCurrency);

    void renamePortfolio(PortfolioId id, String newName);

    void deletePortfolio(PortfolioId id);

    List<Portfolio> listPortfolios();

    /**
     * The portfolio to work with when the user has not picked one.
     *
     * <p>Creates one on first use so a fresh installation has somewhere to book the first trade.
     */
    Portfolio defaultPortfolio();
}
