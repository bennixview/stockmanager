package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;

/**
 * Driven adapter implementing {@link PortfolioRepository} on JPA.
 *
 * <p>A portfolio is written as one aggregate: positions and their tranches are replaced wholesale
 * rather than diffed, which keeps the adapter simple and matches how the domain hands them over.
 * The transaction boundary sits here because one save is one consistent change.
 */
@Component
@Transactional(readOnly = true)
class PortfolioPersistenceAdapter implements PortfolioRepository {

    private final SpringDataPortfolioRepository repository;

    PortfolioPersistenceAdapter(SpringDataPortfolioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Portfolio save(Portfolio portfolio) {
        PortfolioJpaEntity entity = repository.findById(portfolio.id().value())
                .orElseGet(() -> new PortfolioJpaEntity(portfolio.id().value(), portfolio.name(),
                        portfolio.baseCurrency().getCurrencyCode(), portfolio.createdAt()));
        entity.setName(portfolio.name());
        entity.setBaseCurrency(portfolio.baseCurrency().getCurrencyCode());
        entity.replacePositions(portfolio.allPositions().stream().map(PortfolioMapper::toEntity).toList());
        repository.save(entity);
        return portfolio;
    }

    @Override
    public Optional<Portfolio> findById(PortfolioId id) {
        return repository.findById(id.value()).map(PortfolioMapper::toDomain);
    }

    @Override
    public List<Portfolio> findAll() {
        return repository.findAll(Sort.by("createdAt")).stream().map(PortfolioMapper::toDomain).toList();
    }

    @Override
    public Optional<Portfolio> findFirst() {
        return repository.findAllBy(Sort.by("createdAt"), Limit.of(1)).stream()
                .findFirst()
                .map(PortfolioMapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(PortfolioId id) {
        repository.deleteById(id.value());
    }

    @Override
    public long count() {
        return repository.count();
    }
}
