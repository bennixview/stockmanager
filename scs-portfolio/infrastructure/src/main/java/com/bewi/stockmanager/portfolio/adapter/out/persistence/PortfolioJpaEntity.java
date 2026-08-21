package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "portfolio")
class PortfolioJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "base_currency", length = 3, nullable = false)
    private String baseCurrency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Optimistic locking: the aggregate is written as a whole, so one version guards all of it. */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("symbol")
    private List<PositionJpaEntity> positions = new ArrayList<>();

    protected PortfolioJpaEntity() {
        // for JPA
    }

    PortfolioJpaEntity(UUID id, String name, String baseCurrency, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.baseCurrency = baseCurrency;
        this.createdAt = createdAt;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getBaseCurrency() {
        return baseCurrency;
    }

    void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    List<PositionJpaEntity> getPositions() {
        return positions;
    }

    void replacePositions(List<PositionJpaEntity> replacements) {
        positions.clear();
        replacements.forEach(position -> {
            position.setPortfolio(this);
            positions.add(position);
        });
    }
}
