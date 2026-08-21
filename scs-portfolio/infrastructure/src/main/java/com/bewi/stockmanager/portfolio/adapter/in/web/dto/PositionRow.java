package com.bewi.stockmanager.portfolio.adapter.in.web.dto;

import java.math.BigDecimal;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort.MarketPrice;
import com.bewi.stockmanager.portfolio.domain.Isin;
import com.bewi.stockmanager.portfolio.domain.PositionValuation;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/** One line of the positions table. */
public record PositionRow(
        String positionId,
        String symbol,
        String name,
        String isin,
        String wkn,
        String currency,
        String quantity,
        BigDecimal averagePrice,
        BigDecimal currentPrice,
        BigDecimal invested,
        BigDecimal marketValue,
        BigDecimal unrealizedGain,
        BigDecimal unrealizedGainPercent,
        BigDecimal realizedGain,
        BigDecimal dayChangePercent,
        boolean priced,
        boolean up) {

    public static PositionRow from(PositionValuation valuation, MarketPrice price) {
        return new PositionRow(
                valuation.positionId().toString(),
                valuation.instrument().symbol().value(),
                valuation.instrument().name(),
                valuation.instrument().isinValue().map(Isin::value).orElse(null),
                valuation.instrument().wknValue().map(Wkn::value).orElse(null),
                valuation.instrument().currency().getCurrencyCode(),
                // stripTrailingZeros alone would render 10 as 1E+1
                valuation.quantity().value().stripTrailingZeros().toPlainString(),
                valuation.averagePrice().rounded(),
                valuation.isPriced() ? valuation.currentPrice().rounded() : null,
                valuation.invested().rounded(),
                valuation.isPriced() ? valuation.marketValue().rounded() : null,
                valuation.isPriced() ? valuation.unrealizedGain().rounded() : null,
                valuation.unrealizedGainPercent(),
                valuation.realizedGain().rounded(),
                price == null ? null : price.changePercent(),
                valuation.isPriced(),
                valuation.isUp());
    }
}
