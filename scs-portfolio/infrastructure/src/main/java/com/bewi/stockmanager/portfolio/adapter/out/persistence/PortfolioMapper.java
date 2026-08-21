package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.util.Currency;
import java.util.List;

import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Isin;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.RealizedTrade;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Tranche;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/**
 * Translates between the aggregate and its rows.
 *
 * <p>Money is stored as an amount plus the position's currency rather than one column per value
 * object, which keeps the schema readable and lets SQL reports sum a column.
 */
final class PortfolioMapper {

    private PortfolioMapper() {
    }

    static Portfolio toDomain(PortfolioJpaEntity entity) {
        List<Position> positions = entity.getPositions().stream().map(PortfolioMapper::toDomain).toList();
        return Portfolio.restore(new PortfolioId(entity.getId()), entity.getName(),
                Currency.getInstance(entity.getBaseCurrency()), entity.getCreatedAt(), positions);
    }

    private static Position toDomain(PositionJpaEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        InstrumentRef instrument = new InstrumentRef(
                Symbol.of(entity.getSymbol()),
                entity.getInstrumentName(),
                entity.getIsin() == null ? null : Isin.of(entity.getIsin()),
                entity.getWkn() == null ? null : Wkn.of(entity.getWkn()),
                currency);

        List<Tranche> tranches = entity.getTranches().stream()
                .map(tranche -> new Tranche(
                        Quantity.of(tranche.getQuantity()),
                        Money.of(tranche.getPricePerShare(), currency),
                        Money.of(tranche.getFee(), currency),
                        tranche.getTradeDate()))
                .toList();

        List<RealizedTrade> realized = entity.getRealizedTrades().stream()
                .map(trade -> new RealizedTrade(
                        Quantity.of(trade.getQuantity()),
                        Money.of(trade.getBuyPricePerShare(), currency),
                        Money.of(trade.getSellPricePerShare(), currency),
                        Money.of(trade.getFees(), currency),
                        trade.getBuyDate(),
                        trade.getSellDate()))
                .toList();

        return Position.restore(new PositionId(entity.getId()), instrument, tranches, realized);
    }

    static PositionJpaEntity toEntity(Position position) {
        PositionJpaEntity entity = new PositionJpaEntity(
                position.id().value(),
                position.symbol().value(),
                position.instrument().name(),
                position.instrument().isinValue().map(Isin::value).orElse(null),
                position.instrument().wknValue().map(Wkn::value).orElse(null),
                position.currency().getCurrencyCode());

        position.tranches().forEach(tranche -> entity.addTranche(new TrancheJpaEntity(
                tranche.quantity().value(),
                tranche.pricePerShare().amount(),
                tranche.fee().amount(),
                tranche.tradeDate())));

        position.realizedTrades().forEach(trade -> entity.addRealizedTrade(new RealizedTradeJpaEntity(
                trade.quantity().value(),
                trade.buyPricePerShare().amount(),
                trade.sellPricePerShare().amount(),
                trade.fees().amount(),
                trade.buyDate(),
                trade.sellDate())));

        return entity;
    }
}
