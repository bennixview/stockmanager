package com.bewi.stockmanager.marketdata.domain;

/** The kind of tradable instrument, as far as this system needs to distinguish them. */
public enum InstrumentType {
    STOCK,
    ETF,
    FUND,
    INDEX,
    BOND,
    CRYPTO,
    OTHER
}
