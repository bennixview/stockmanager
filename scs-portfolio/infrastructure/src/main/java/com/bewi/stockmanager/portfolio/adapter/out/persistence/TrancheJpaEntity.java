package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** An open purchase lot. Amounts are in the position's currency. */
@Entity
@Table(name = "tranche")
class TrancheJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionJpaEntity position;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "price_per_share", nullable = false, precision = 19, scale = 6)
    private BigDecimal pricePerShare;

    @Column(name = "fee", nullable = false, precision = 19, scale = 6)
    private BigDecimal fee;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    protected TrancheJpaEntity() {
        // for JPA
    }

    TrancheJpaEntity(BigDecimal quantity, BigDecimal pricePerShare, BigDecimal fee, LocalDate tradeDate) {
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.fee = fee;
        this.tradeDate = tradeDate;
    }

    BigDecimal getQuantity() {
        return quantity;
    }

    BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    BigDecimal getFee() {
        return fee;
    }

    LocalDate getTradeDate() {
        return tradeDate;
    }

    void setPosition(PositionJpaEntity position) {
        this.position = position;
    }
}
