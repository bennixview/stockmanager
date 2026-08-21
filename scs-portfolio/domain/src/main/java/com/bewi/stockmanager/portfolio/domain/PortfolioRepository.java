package com.bewi.stockmanager.portfolio.domain;

import java.util.List;
import java.util.Optional;

import org.jmolecules.ddd.annotation.Repository;

/** Collection-like access to portfolios, saved and loaded as whole aggregates. */
@Repository
public interface PortfolioRepository {

    Portfolio save(Portfolio portfolio);

    Optional<Portfolio> findById(PortfolioId id);

    List<Portfolio> findAll();

    /** The portfolio to show when none was asked for; empty on a fresh installation. */
    Optional<Portfolio> findFirst();

    void delete(PortfolioId id);

    long count();
}
