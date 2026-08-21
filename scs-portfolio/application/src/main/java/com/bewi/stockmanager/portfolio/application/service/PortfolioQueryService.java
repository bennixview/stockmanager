package com.bewi.stockmanager.portfolio.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort.MarketPrice;
import com.bewi.stockmanager.portfolio.domain.ExchangeRates;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;
import com.bewi.stockmanager.portfolio.domain.PortfolioValuation;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/**
 * The read side: a portfolio plus what the market currently says it is worth.
 *
 * <p>Prices are fetched in one batch per request. Valuation itself is the aggregate's job, so this
 * service only supplies the numbers it needs.
 */
public class PortfolioQueryService implements ViewPortfolio {

    private final PortfolioRepository portfolios;
    private final MarketDataPort marketData;
    private final ExchangeRates exchangeRates;
    private final ManagePortfolios managePortfolios;

    public PortfolioQueryService(PortfolioRepository portfolios, MarketDataPort marketData,
            ExchangeRates exchangeRates, ManagePortfolios managePortfolios) {
        this.portfolios = Objects.requireNonNull(portfolios);
        this.marketData = Objects.requireNonNull(marketData);
        this.exchangeRates = Objects.requireNonNull(exchangeRates);
        this.managePortfolios = Objects.requireNonNull(managePortfolios);
    }

    @Override
    public Overview overview(PortfolioId portfolioId) {
        return overviewOf(portfolios.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioId)));
    }

    @Override
    public Overview defaultOverview() {
        return overviewOf(managePortfolios.defaultPortfolio());
    }

    @Override
    public PositionDetails position(PortfolioId portfolioId, PositionId positionId) {
        Portfolio portfolio = portfolios.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
        Position position = portfolio.position(positionId);
        MarketPrice price = marketData.priceFor(position.symbol()).orElse(null);
        return new PositionDetails(portfolio, position,
                price == null ? position.valuateWithoutPrice() : position.valuate(price.price()), price);
    }

    private Overview overviewOf(Portfolio portfolio) {
        Map<Symbol, MarketPrice> prices = marketData.pricesFor(portfolio.watchedSymbols());
        PortfolioValuation valuation = portfolio.valuate(pricesByCurrency(prices), exchangeRates);
        return new Overview(portfolio, valuation, prices, portfolios.findAll());
    }

    private static Map<Symbol, Money> pricesByCurrency(Map<Symbol, MarketPrice> prices) {
        Map<Symbol, Money> plain = new HashMap<>(prices.size());
        prices.forEach((symbol, price) -> plain.put(symbol, price.price()));
        return plain;
    }

    /** Convenience for callers that only need the list, e.g. a portfolio switcher. */
    public List<Portfolio> allPortfolios() {
        return portfolios.findAll();
    }
}
