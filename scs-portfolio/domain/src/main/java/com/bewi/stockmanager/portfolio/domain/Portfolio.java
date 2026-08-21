package com.bewi.stockmanager.portfolio.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A collection of positions kept in one base currency.
 *
 * <p>The aggregate root: buying and selling goes through here, so the invariant that a portfolio
 * holds at most one position per instrument, and that a position never goes short, is enforced in
 * one place.
 */
@AggregateRoot
public class Portfolio {

    @Identity
    private final PortfolioId id;
    private final Currency baseCurrency;
    private final Instant createdAt;
    private final List<Position> positions = new ArrayList<>();
    private String name;

    public Portfolio(PortfolioId id, String name, Currency baseCurrency, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.name = requireName(name);
    }

    public static Portfolio open(String name, Currency baseCurrency, Instant createdAt) {
        return new Portfolio(PortfolioId.generate(), name, baseCurrency, createdAt);
    }

    /** Rebuilds a portfolio from stored state; used by repositories. */
    public static Portfolio restore(PortfolioId id, String name, Currency baseCurrency, Instant createdAt,
            List<Position> positions) {
        Portfolio portfolio = new Portfolio(id, name, baseCurrency, createdAt);
        portfolio.positions.addAll(positions);
        return portfolio;
    }

    public PortfolioId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Currency baseCurrency() {
        return baseCurrency;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public void rename(String newName) {
        this.name = requireName(newName);
    }

    /** Open positions, largest first by what was put into them. */
    public List<Position> positions() {
        return positions.stream()
                .filter(position -> !position.isClosed())
                .sorted(Comparator.comparing((Position position) -> position.invested().amount()).reversed())
                .toList();
    }

    /** Positions that have been sold off completely but still carry their realized gains. */
    public List<Position> closedPositions() {
        return positions.stream().filter(Position::isClosed).toList();
    }

    public List<Position> allPositions() {
        return List.copyOf(positions);
    }

    public Optional<Position> findPosition(PositionId positionId) {
        return positions.stream().filter(position -> position.id().equals(positionId)).findFirst();
    }

    public Optional<Position> findPosition(Symbol symbol) {
        return positions.stream().filter(position -> position.symbol().equals(symbol)).findFirst();
    }

    public Position position(PositionId positionId) {
        return findPosition(positionId).orElseThrow(() -> new PositionNotFoundException(positionId));
    }

    /**
     * Buys shares, extending an existing position in the same instrument or opening a new one.
     *
     * @return the position the shares went into
     */
    public Position buy(InstrumentRef instrument, Quantity quantity, Money pricePerShare, Money fee,
            LocalDate tradeDate) {
        Position position = findPosition(instrument.symbol()).orElseGet(() -> {
            Position fresh = new Position(PositionId.generate(), instrument);
            positions.add(fresh);
            return fresh;
        });
        position.buy(quantity, pricePerShare, fee, tradeDate);
        return position;
    }

    /**
     * Sells shares of an instrument held.
     *
     * @return the round trips this sale closed
     */
    public List<RealizedTrade> sell(Symbol symbol, Quantity quantity, Money pricePerShare, Money fee,
            LocalDate tradeDate) {
        Position position = findPosition(symbol).orElseThrow(() -> new PositionNotFoundException(symbol));
        return position.sell(quantity, pricePerShare, fee, tradeDate);
    }

    /** Drops a position and its history, for corrections rather than for sales. */
    public void removePosition(PositionId positionId) {
        if (!positions.removeIf(position -> position.id().equals(positionId))) {
            throw new PositionNotFoundException(positionId);
        }
    }

    /**
     * Values every position at the given prices and adds the results up in the base currency.
     *
     * @param prices       the current price per symbol, as far as the market knows them
     * @param exchangeRates used where an instrument is not traded in the base currency
     */
    public PortfolioValuation valuate(java.util.Map<Symbol, Money> prices, ExchangeRates exchangeRates) {
        Money invested = Money.zero(baseCurrency);
        Money marketValue = Money.zero(baseCurrency);
        Money realized = Money.zero(baseCurrency);
        List<PositionValuation> valued = new ArrayList<>();
        List<PositionValuation> unpriced = new ArrayList<>();

        for (Position position : positions) {
            Money price = prices.get(position.symbol());
            PositionValuation valuation = price == null
                    ? position.valuateWithoutPrice()
                    : position.valuate(price);

            Optional<Money> investedInBase = exchangeRates.convert(valuation.invested(), baseCurrency);
            Optional<Money> valueInBase = valuation.marketValueIfPriced()
                    .flatMap(value -> exchangeRates.convert(value, baseCurrency));
            Optional<Money> realizedInBase = exchangeRates.convert(valuation.realizedGain(), baseCurrency);

            if (valuation.isPriced() && investedInBase.isPresent() && valueInBase.isPresent()) {
                if (!position.isClosed()) {
                    invested = invested.plus(investedInBase.get());
                    marketValue = marketValue.plus(valueInBase.get());
                }
                realized = realized.plus(realizedInBase.orElse(Money.zero(baseCurrency)));
                valued.add(valuation);
            } else if (position.isClosed()) {
                realized = realized.plus(realizedInBase.orElse(Money.zero(baseCurrency)));
            } else {
                unpriced.add(valuation);
            }
        }

        Money unrealized = marketValue.minus(invested);
        java.math.BigDecimal percent = invested.isZero()
                ? java.math.BigDecimal.ZERO
                : invested.percentageChangeTo(marketValue);
        return new PortfolioValuation(id, baseCurrency, invested, marketValue, unrealized, percent, realized, valued,
                unpriced);
    }

    /** Every symbol the portfolio needs a price for. */
    public List<Symbol> watchedSymbols() {
        return positions.stream().filter(position -> !position.isClosed()).map(Position::symbol).distinct().toList();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A portfolio needs a name");
        }
        return name.trim();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Portfolio portfolio && id.equals(portfolio.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "%s (%d positions)".formatted(name, positions().size());
    }
}
