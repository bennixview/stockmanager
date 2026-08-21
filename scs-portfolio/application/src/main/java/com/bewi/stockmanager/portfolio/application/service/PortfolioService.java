package com.bewi.stockmanager.portfolio.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.RealizedTrade;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/**
 * The write side: creating portfolios and booking trades.
 *
 * <p>Each method loads one aggregate, lets it decide, and saves it again. All the rules about what
 * a trade does to a position live in the domain; what is left here is orchestration.
 */
public class PortfolioService implements ManagePortfolios, TradeInstruments {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);
    private static final String DEFAULT_PORTFOLIO_NAME = "Mein Depot";

    private final PortfolioRepository portfolios;
    private final MarketDataPort marketData;
    private final Currency defaultCurrency;
    private final Clock clock;

    public PortfolioService(PortfolioRepository portfolios, MarketDataPort marketData, Currency defaultCurrency,
            Clock clock) {
        this.portfolios = Objects.requireNonNull(portfolios);
        this.marketData = Objects.requireNonNull(marketData);
        this.defaultCurrency = Objects.requireNonNull(defaultCurrency);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Portfolio createPortfolio(String name, Currency baseCurrency) {
        Portfolio portfolio = Portfolio.open(name, baseCurrency == null ? defaultCurrency : baseCurrency,
                clock.instant());
        log.info("Created portfolio '{}' ({})", portfolio.name(), portfolio.id());
        return portfolios.save(portfolio);
    }

    @Override
    public void renamePortfolio(PortfolioId id, String newName) {
        Portfolio portfolio = load(id);
        portfolio.rename(newName);
        portfolios.save(portfolio);
    }

    @Override
    public void deletePortfolio(PortfolioId id) {
        portfolios.delete(id);
    }

    @Override
    public List<Portfolio> listPortfolios() {
        return portfolios.findAll();
    }

    @Override
    public Portfolio defaultPortfolio() {
        return portfolios.findFirst()
                .orElseGet(() -> createPortfolio(DEFAULT_PORTFOLIO_NAME, defaultCurrency));
    }

    @Override
    public Position buy(BuyOrder order) {
        Portfolio portfolio = load(order.portfolioId());
        Symbol symbol = order.ticker();
        InstrumentRef instrument = resolveInstrument(symbol, order.name(), order.currency(), portfolio);
        Money price = Money.of(order.pricePerShare(), instrument.currency());
        Money fee = Money.of(Objects.requireNonNullElse(order.fee(), BigDecimal.ZERO), instrument.currency());

        Position position = portfolio.buy(instrument, Quantity.of(order.quantity()), price, fee,
                tradeDateOrToday(order.tradeDate()));
        portfolios.save(portfolio);
        log.info("Bought {} {} at {} in portfolio {}", order.quantity(), symbol, price, portfolio.id());
        return position;
    }

    @Override
    public List<RealizedTrade> sell(SellOrder order) {
        Portfolio portfolio = load(order.portfolioId());
        Symbol symbol = order.ticker();
        Currency currency = portfolio.findPosition(symbol)
                .map(Position::currency)
                .orElse(portfolio.baseCurrency());
        Money price = Money.of(order.pricePerShare(), currency);
        Money fee = Money.of(Objects.requireNonNullElse(order.fee(), BigDecimal.ZERO), currency);

        List<RealizedTrade> closed = portfolio.sell(symbol, Quantity.of(order.quantity()), price, fee,
                tradeDateOrToday(order.tradeDate()));
        portfolios.save(portfolio);
        log.info("Sold {} {} at {} in portfolio {}", order.quantity(), symbol, price, portfolio.id());
        return closed;
    }

    @Override
    public void removePosition(PortfolioId portfolioId, PositionId positionId) {
        Portfolio portfolio = load(portfolioId);
        portfolio.removePosition(positionId);
        portfolios.save(portfolio);
    }

    /**
     * What we know about the instrument being traded.
     *
     * <p>Preference goes to a position already held, then to the market data catalogue, and only
     * then to what the order itself said - so a trade still books when the other system is down.
     */
    private InstrumentRef resolveInstrument(Symbol symbol, String name, String currencyCode, Portfolio portfolio) {
        Optional<InstrumentRef> held = portfolio.findPosition(symbol).map(Position::instrument);
        if (held.isPresent()) {
            return held.get();
        }
        Optional<InstrumentRef> known = marketData.bySymbol(symbol);
        if (known.isPresent()) {
            return known.get();
        }
        Currency currency = currencyCode == null || currencyCode.isBlank()
                ? portfolio.baseCurrency()
                : Currency.getInstance(currencyCode);
        log.info("Instrument {} is not in the catalogue, booking it as {}", symbol, currency.getCurrencyCode());
        return InstrumentRef.of(symbol, name, currency);
    }

    private LocalDate tradeDateOrToday(LocalDate tradeDate) {
        return tradeDate == null ? LocalDate.now(clock) : tradeDate;
    }

    private Portfolio load(PortfolioId id) {
        return portfolios.findById(id).orElseThrow(() -> new PortfolioNotFoundException(id));
    }
}
