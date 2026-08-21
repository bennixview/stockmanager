package com.bewi.stockmanager.portfolio.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.RealizedTrade;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/** Driving port for booking trades. */
public interface TradeInstruments {

    Position buy(BuyOrder order);

    List<RealizedTrade> sell(SellOrder order);

    /** Removes a position outright - a correction, not a sale. */
    void removePosition(PortfolioId portfolioId, PositionId positionId);

    /**
     * A purchase.
     *
     * @param symbol   the ticker the market data system knows the instrument by
     * @param name     what to call the instrument if the catalogue does not know it; the catalogue
     *                 wins when it does
     * @param currency the currency the instrument is traded in; falls back to the catalogue entry
     */
    record BuyOrder(
            PortfolioId portfolioId,
            String symbol,
            String name,
            BigDecimal quantity,
            BigDecimal pricePerShare,
            BigDecimal fee,
            String currency,
            LocalDate tradeDate) {

        public Symbol ticker() {
            return Symbol.of(symbol);
        }
    }

    /** A sale of shares already held. */
    record SellOrder(
            PortfolioId portfolioId,
            String symbol,
            BigDecimal quantity,
            BigDecimal pricePerShare,
            BigDecimal fee,
            LocalDate tradeDate) {

        public Symbol ticker() {
            return Symbol.of(symbol);
        }
    }
}
