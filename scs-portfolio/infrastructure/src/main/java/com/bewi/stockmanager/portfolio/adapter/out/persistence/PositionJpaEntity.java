package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_position")
class PositionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfolioJpaEntity portfolio;

    @Column(name = "symbol", length = 20, nullable = false)
    private String symbol;

    @Column(name = "instrument_name", nullable = false)
    private String instrumentName;

    @Column(name = "isin", length = 12)
    private String isin;

    @Column(name = "wkn", length = 6)
    private String wkn;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("tradeDate")
    private List<TrancheJpaEntity> tranches = new ArrayList<>();

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sellDate")
    private List<RealizedTradeJpaEntity> realizedTrades = new ArrayList<>();

    protected PositionJpaEntity() {
        // for JPA
    }

    PositionJpaEntity(UUID id, String symbol, String instrumentName, String isin, String wkn, String currency) {
        this.id = id;
        this.symbol = symbol;
        this.instrumentName = instrumentName;
        this.isin = isin;
        this.wkn = wkn;
        this.currency = currency;
    }

    UUID getId() {
        return id;
    }

    String getSymbol() {
        return symbol;
    }

    String getInstrumentName() {
        return instrumentName;
    }

    String getIsin() {
        return isin;
    }

    String getWkn() {
        return wkn;
    }

    String getCurrency() {
        return currency;
    }

    List<TrancheJpaEntity> getTranches() {
        return tranches;
    }

    List<RealizedTradeJpaEntity> getRealizedTrades() {
        return realizedTrades;
    }

    void setPortfolio(PortfolioJpaEntity portfolio) {
        this.portfolio = portfolio;
    }

    void addTranche(TrancheJpaEntity tranche) {
        tranche.setPosition(this);
        tranches.add(tranche);
    }

    void addRealizedTrade(RealizedTradeJpaEntity trade) {
        trade.setPosition(this);
        realizedTrades.add(trade);
    }
}
