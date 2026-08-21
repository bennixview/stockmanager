package com.bewi.stockmanager.portfolio.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Everything held in one instrument: the open lots and the trades already closed.
 *
 * <p>Sales consume the oldest lot first (FIFO), which is what German tax law assumes and what makes
 * the realized gain of a partial sale well defined.
 */
@Entity
public class Position {

    @Identity
    private final PositionId id;
    private final InstrumentRef instrument;
    private final List<Tranche> tranches = new ArrayList<>();
    private final List<RealizedTrade> realizedTrades = new ArrayList<>();

    public Position(PositionId id, InstrumentRef instrument) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
    }

    /** Rebuilds a position from stored state; used by repositories, not by callers in the domain. */
    public static Position restore(PositionId id, InstrumentRef instrument, List<Tranche> tranches,
            List<RealizedTrade> realizedTrades) {
        Position position = new Position(id, instrument);
        position.tranches.addAll(tranches);
        position.realizedTrades.addAll(realizedTrades);
        position.sortTranches();
        return position;
    }

    public PositionId id() {
        return id;
    }

    public InstrumentRef instrument() {
        return instrument;
    }

    public Symbol symbol() {
        return instrument.symbol();
    }

    public List<Tranche> tranches() {
        return List.copyOf(tranches);
    }

    public List<RealizedTrade> realizedTrades() {
        return List.copyOf(realizedTrades);
    }

    void buy(Quantity quantity, Money pricePerShare, Money fee, LocalDate tradeDate) {
        requireMatchingCurrency(pricePerShare);
        tranches.add(Tranche.of(quantity, pricePerShare, fee, tradeDate));
        sortTranches();
    }

    /**
     * Sells shares off the oldest lots first and records what that realized.
     *
     * @return the trades this sale closed, oldest lot first
     */
    List<RealizedTrade> sell(Quantity quantity, Money pricePerShare, Money fee, LocalDate tradeDate) {
        requireMatchingCurrency(pricePerShare);
        Money sellFee = fee == null ? Money.zero(currency()) : fee;
        requireMatchingCurrency(sellFee);
        Quantity held = quantity();
        if (quantity.isGreaterThan(held)) {
            throw new InsufficientSharesException(symbol(), held, quantity);
        }

        List<RealizedTrade> closed = new ArrayList<>();
        Quantity remaining = quantity;
        List<Tranche> keep = new ArrayList<>();

        for (Tranche tranche : tranches) {
            if (remaining.isZero()) {
                keep.add(tranche);
                continue;
            }
            Quantity fromThisLot = remaining.isGreaterThan(tranche.quantity()) ? tranche.quantity() : remaining;
            Money sellFeeShare = proportionalFee(sellFee, fromThisLot, quantity);
            closed.add(new RealizedTrade(
                    fromThisLot,
                    tranche.pricePerShare(),
                    pricePerShare,
                    tranche.feeFor(fromThisLot).plus(sellFeeShare),
                    tranche.tradeDate(),
                    tradeDate));
            remaining = remaining.minus(fromThisLot);
            Quantity leftInLot = tranche.quantity().minus(fromThisLot);
            if (!leftInLot.isZero()) {
                keep.add(tranche.reduceTo(leftInLot));
            }
        }

        tranches.clear();
        tranches.addAll(keep);
        sortTranches();
        realizedTrades.addAll(closed);
        return List.copyOf(closed);
    }

    /** Shares currently held. */
    public Quantity quantity() {
        return tranches.stream().map(Tranche::quantity).reduce(Quantity.ZERO, Quantity::plus);
    }

    /** What the shares still held cost, fees included. */
    public Money invested() {
        return tranches.stream().map(Tranche::cost).reduce(Money.zero(currency()), Money::plus);
    }

    /** Average price paid per share still held, fees included. */
    public Money averagePrice() {
        Quantity quantity = quantity();
        if (quantity.isZero()) {
            return Money.zero(currency());
        }
        return Money.of(invested().amount().divide(quantity.value(), Money.SCALE, RoundingMode.HALF_UP), currency());
    }

    /** Profit already banked on this instrument. */
    public Money realizedGain() {
        return realizedTrades.stream().map(RealizedTrade::gain).reduce(Money.zero(currency()), Money::plus);
    }

    public boolean isClosed() {
        return quantity().isZero();
    }

    public LocalDate firstTradeDate() {
        return tranches.stream().map(Tranche::tradeDate).min(Comparator.naturalOrder())
                .orElseGet(() -> realizedTrades.stream().map(RealizedTrade::buyDate).min(Comparator.naturalOrder())
                        .orElse(LocalDate.EPOCH));
    }

    /** What the position is worth at the given price, and how far that is from what it cost. */
    public PositionValuation valuate(Money currentPrice) {
        requireMatchingCurrency(currentPrice);
        Money marketValue = quantity().at(currentPrice);
        Money invested = invested();
        Money unrealized = marketValue.minus(invested);
        BigDecimal percent = invested.isZero() ? BigDecimal.ZERO : invested.percentageChangeTo(marketValue);
        return new PositionValuation(id, instrument, quantity(), averagePrice(), currentPrice, invested, marketValue,
                unrealized, percent, realizedGain());
    }

    /** Valuation without a price - everything that does not need one still adds up. */
    public PositionValuation valuateWithoutPrice() {
        return new PositionValuation(id, instrument, quantity(), averagePrice(), null, invested(), null, null, null,
                realizedGain());
    }

    public java.util.Currency currency() {
        return instrument.currency();
    }

    private void sortTranches() {
        tranches.sort(Comparator.comparing(Tranche::tradeDate));
    }

    private void requireMatchingCurrency(Money money) {
        if (!money.currency().equals(currency())) {
            throw new IllegalArgumentException("%s is traded in %s, not in %s"
                    .formatted(symbol(), currency().getCurrencyCode(), money.currency().getCurrencyCode()));
        }
    }

    /** The share of a sell fee that belongs to the shares taken out of one lot. */
    private static Money proportionalFee(Money totalFee, Quantity part, Quantity whole) {
        if (whole.isZero()) {
            return Money.zero(totalFee.currency());
        }
        return Money.of(totalFee.amount().multiply(part.value())
                .divide(whole.value(), Money.SCALE, RoundingMode.HALF_UP), totalFee.currency());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Position position && id.equals(position.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "%s: %s shares".formatted(instrument, quantity());
    }
}
