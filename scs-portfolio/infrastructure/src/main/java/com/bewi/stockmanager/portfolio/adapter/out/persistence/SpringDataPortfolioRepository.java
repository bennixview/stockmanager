package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPortfolioRepository extends JpaRepository<PortfolioJpaEntity, UUID> {

    List<PortfolioJpaEntity> findAllBy(Sort sort, Limit limit);
}
