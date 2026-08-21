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

/** A closed round trip. Amounts are in the position's currency. */
@Entity
@Table(name = "realized_trade")
class RealizedTradeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionJpaEntity position;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "buy_price_per_share", nullable = false, precision = 19, scale = 6)
    private BigDecimal buyPricePerShare;

    @Column(name = "sell_price_per_share", nullable = false, precision = 19, scale = 6)
    private BigDecimal sellPricePerShare;

    @Column(name = "fees", nullable = false, precision = 19, scale = 6)
    private BigDecimal fees;

    @Column(name = "buy_date", nullable = false)
    private LocalDate buyDate;

    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    protected RealizedTradeJpaEntity() {
        // for JPA
    }

    RealizedTradeJpaEntity(BigDecimal quantity, BigDecimal buyPricePerShare, BigDecimal sellPricePerShare,
            BigDecimal fees, LocalDate buyDate, LocalDate sellDate) {
        this.quantity = quantity;
        this.buyPricePerShare = buyPricePerShare;
        this.sellPricePerShare = sellPricePerShare;
        this.fees = fees;
        this.buyDate = buyDate;
        this.sellDate = sellDate;
    }

    BigDecimal getQuantity() {
        return quantity;
    }

    BigDecimal getBuyPricePerShare() {
        return buyPricePerShare;
    }

    BigDecimal getSellPricePerShare() {
        return sellPricePerShare;
    }

    BigDecimal getFees() {
        return fees;
    }

    LocalDate getBuyDate() {
        return buyDate;
    }

    LocalDate getSellDate() {
        return sellDate;
    }

    void setPosition(PositionJpaEntity position) {
        this.position = position;
    }
}
