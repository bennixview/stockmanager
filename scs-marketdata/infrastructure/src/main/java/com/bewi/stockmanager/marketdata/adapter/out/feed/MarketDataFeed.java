package com.bewi.stockmanager.marketdata.adapter.out.feed;

import com.bewi.stockmanager.marketdata.application.port.out.PriceHistoryFeed;
import com.bewi.stockmanager.marketdata.application.port.out.QuoteFeed;

/**
 * A provider that serves both driven feed ports.
 *
 * <p>The application layer keeps the two ports apart because they are used at different moments;
 * every provider we actually integrate happens to speak one protocol for both, and this interface
 * lets a single adapter instance be wired to both ports.
 */
public interface MarketDataFeed extends QuoteFeed, PriceHistoryFeed {
}
