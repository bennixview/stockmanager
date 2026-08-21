package com.bewi.stockmanager.marketdata.application.port.out;

import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;

/** Driven port for historical candles. */
public interface PriceHistoryFeed {

    PriceHistory history(Instrument instrument, HistoryRange range);
}
