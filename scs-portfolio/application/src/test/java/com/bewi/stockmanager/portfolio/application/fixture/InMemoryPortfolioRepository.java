package com.bewi.stockmanager.portfolio.application.fixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;

/** Repository port backed by a map, so use cases can be tested without a database. */
public class InMemoryPortfolioRepository implements PortfolioRepository {

    private final Map<PortfolioId, Portfolio> portfolios = new LinkedHashMap<>();
    private int saves;

    @Override
    public Portfolio save(Portfolio portfolio) {
        portfolios.put(portfolio.id(), portfolio);
        saves++;
        return portfolio;
    }

    @Override
    public Optional<Portfolio> findById(PortfolioId id) {
        return Optional.ofNullable(portfolios.get(id));
    }

    @Override
    public List<Portfolio> findAll() {
        return new ArrayList<>(portfolios.values());
    }

    @Override
    public Optional<Portfolio> findFirst() {
        return portfolios.values().stream().findFirst();
    }

    @Override
    public void delete(PortfolioId id) {
        portfolios.remove(id);
    }

    @Override
    public long count() {
        return portfolios.size();
    }

    /** How often the aggregate was written, to check that a use case persists what it changed. */
    public int saveCount() {
        return saves;
    }
}
