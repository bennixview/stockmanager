package com.bewi.stockmanager.marketdata.domain;

/** Whether a quote was taken while its market was trading. */
public enum MarketState {
    PRE_MARKET,
    OPEN,
    POST_MARKET,
    CLOSED,
    UNKNOWN
}
