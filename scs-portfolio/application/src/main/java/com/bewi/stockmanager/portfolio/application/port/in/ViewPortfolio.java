package com.bewi.stockmanager.portfolio.application.port.in;

import java.util.List;
import java.util.Map;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort.MarketPrice;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioValuation;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.PositionValuation;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/** Driving port for reading a portfolio, priced with whatever the market data system can supply. */
public interface ViewPortfolio {

    Overview overview(PortfolioId portfolioId);

    /** Overview of the default portfolio, creating it if this is a fresh installation. */
    Overview defaultOverview();

    PositionDetails position(PortfolioId portfolioId, PositionId positionId);

    /** A portfolio and its current valuation. */
    record Overview(Portfolio portfolio, PortfolioValuation valuation, Map<Symbol, MarketPrice> prices,
            List<Portfolio> allPortfolios) {
    }

    /** One position with its valuation and the trades behind it. */
    record PositionDetails(Portfolio portfolio, Position position, PositionValuation valuation, MarketPrice price) {
    }
}
