package com.bewi.stockmanager.portfolio.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;

/** The position detail page: the row plus the trades behind it. */
public record PositionDetailView(
        String portfolioId,
        String portfolioName,
        PositionRow position,
        List<TrancheRow> tranches,
        List<RealizedTradeRow> realizedTrades) {

    public static PositionDetailView from(ViewPortfolio.PositionDetails details) {
        return new PositionDetailView(
                details.portfolio().id().toString(),
                details.portfolio().name(),
                PositionRow.from(details.valuation(), details.price()),
                details.position().tranches().stream()
                        .map(tranche -> new TrancheRow(
                                tranche.quantity().value().stripTrailingZeros().toPlainString(),
                                tranche.pricePerShare().rounded(),
                                tranche.fee().rounded(),
                                tranche.cost().rounded(),
                                tranche.tradeDate()))
                        .toList(),
                details.position().realizedTrades().stream()
                        .map(trade -> new RealizedTradeRow(
                                trade.quantity().value().stripTrailingZeros().toPlainString(),
                                trade.buyPricePerShare().rounded(),
                                trade.sellPricePerShare().rounded(),
                                trade.fees().rounded(),
                                trade.gain().rounded(),
                                !trade.gain().isNegative(),
                                trade.buyDate(),
                                trade.sellDate()))
                        .toList());
    }

    /** An open purchase lot. */
    public record TrancheRow(String quantity, BigDecimal pricePerShare, BigDecimal fee, BigDecimal cost,
            LocalDate tradeDate) {
    }

    /** A closed round trip. */
    public record RealizedTradeRow(String quantity, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal fees,
            BigDecimal gain, boolean up, LocalDate buyDate, LocalDate sellDate) {
    }
}
