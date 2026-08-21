package com.bewi.stockmanager.marketdata.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Database representation of an instrument - a detail of this adapter, never leaves it. */
@Entity
@Table(name = "instrument")
class InstrumentJpaEntity {

    @Id
    @Column(name = "symbol", length = 20, nullable = false)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "isin", length = 12)
    private String isin;

    @Column(name = "wkn", length = 6)
    private String wkn;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "exchange", length = 40)
    private String exchange;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    protected InstrumentJpaEntity() {
        // for JPA
    }

    InstrumentJpaEntity(String symbol, String name, String isin, String wkn, String currency, String exchange,
            String type) {
        this.symbol = symbol;
        this.name = name;
        this.isin = isin;
        this.wkn = wkn;
        this.currency = currency;
        this.exchange = exchange;
        this.type = type;
    }

    String getSymbol() {
        return symbol;
    }

    String getName() {
        return name;
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

    String getExchange() {
        return exchange;
    }

    String getType() {
        return type;
    }
}
