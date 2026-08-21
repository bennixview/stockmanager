package com.bewi.stockmanager.portfolio.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioValuation;

/**
 * Everything one portfolio page shows, flattened for the template.
 *
 * <p>Views are the web adapter's own model: templates never reach into the aggregate, so the domain
 * can be refactored without breaking a page.
 */
public record PortfolioView(
        String id,
        String name,
        String currency,
        BigDecimal invested,
        BigDecimal marketValue,
        BigDecimal unrealizedGain,
        BigDecimal unrealizedGainPercent,
        BigDecimal realizedGain,
        boolean up,
        boolean complete,
        List<PositionRow> positions,
        List<PositionRow> unpricedPositions,
        List<PositionRow> closedPositions,
        List<PortfolioLink> portfolios) {

    public static PortfolioView from(ViewPortfolio.Overview overview) {
        Portfolio portfolio = overview.portfolio();
        PortfolioValuation valuation = overview.valuation();
        return new PortfolioView(
                portfolio.id().toString(),
                portfolio.name(),
                portfolio.baseCurrency().getCurrencyCode(),
                valuation.invested().rounded(),
                valuation.marketValue().rounded(),
                valuation.unrealizedGain().rounded(),
                valuation.unrealizedGainPercent(),
                valuation.realizedGain().rounded(),
                valuation.isUp(),
                valuation.isComplete(),
                valuation.positions().stream()
                        .filter(position -> !position.quantity().isZero())
                        .map(position -> PositionRow.from(position, overview.prices().get(position.instrument().symbol())))
                        .toList(),
                valuation.unpricedPositions().stream()
                        .map(position -> PositionRow.from(position, null))
                        .toList(),
                portfolio.closedPositions().stream()
                        .map(position -> PositionRow.from(position.valuateWithoutPrice(), null))
                        .toList(),
                overview.allPortfolios().stream()
                        .map(other -> new PortfolioLink(other.id().toString(), other.name(),
                                other.id().equals(portfolio.id())))
                        .toList());
    }

    /**
     * The priced positions as a JSON array for the allocation chart.
     *
     * <p>Built here rather than in the template so the page carries no string concatenation.
     */
    public String allocationJson() {
        return positions.stream()
                .filter(PositionRow::priced)
                .map(row -> "{\"label\":\"%s\",\"value\":%s}"
                        .formatted(row.symbol(), row.marketValue().toPlainString()))
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    public boolean isEmpty() {
        return positions.isEmpty() && unpricedPositions.isEmpty() && closedPositions.isEmpty();
    }

    /** An entry in the portfolio switcher. */
    public record PortfolioLink(String id, String name, boolean active) {
    }
}
