package com.bewi.stockmanager.portfolio.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bewi.stockmanager.portfolio.application.fixture.InMemoryPortfolioRepository;
import com.bewi.stockmanager.portfolio.application.fixture.StubMarketData;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments.BuyOrder;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments.SellOrder;
import com.bewi.stockmanager.portfolio.domain.InsufficientSharesException;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.Symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PortfolioServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final Symbol SAP = Symbol.of("SAP.DE");

    private InMemoryPortfolioRepository portfolios;
    private StubMarketData marketData;
    private PortfolioService service;

    @BeforeEach
    void setUp() {
        portfolios = new InMemoryPortfolioRepository();
        marketData = new StubMarketData().knows(StubMarketData.instrument("SAP.DE", "SAP SE", "EUR"));
        service = new PortfolioService(portfolios, marketData, EUR, CLOCK);
    }

    @Test
    void createsADefaultPortfolioOnFirstUseAndReusesItAfterwards() {
        Portfolio first = service.defaultPortfolio();
        Portfolio second = service.defaultPortfolio();

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(portfolios.count()).isEqualTo(1);
        assertThat(first.baseCurrency()).isEqualTo(EUR);
    }

    @Test
    void booksABuyAndPersistsTheAggregate() {
        Portfolio portfolio = service.createPortfolio("Depot", EUR);

        Position position = service.buy(order(portfolio.id(), "SAP.DE", "10", "100"));

        assertThat(position.quantity()).isEqualTo(Quantity.of(10));
        assertThat(portfolios.findById(portfolio.id()).orElseThrow().positions()).hasSize(1);
    }

    @Test
    void takesNameAndCurrencyFromTheMarketDataCatalogue() {
        marketData.knows(StubMarketData.instrument("AAPL", "Apple Inc.", "USD"));
        Portfolio portfolio = service.createPortfolio("Depot", EUR);

        Position position = service.buy(order(portfolio.id(), "AAPL", "2", "200"));

        assertThat(position.instrument().name()).isEqualTo("Apple Inc.");
        assertThat(position.currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void booksTheTradeEvenWhenTheMarketDataSystemIsDown() {
        Portfolio portfolio = service.createPortfolio("Depot", EUR);
        service = new PortfolioService(portfolios, new StubMarketData().unavailable(), EUR, CLOCK);

        Position position = service.buy(order(portfolio.id(), "NEWCO", "5", "10"));

        assertThat(position.instrument().name()).isEqualTo("NEWCO");
        assertThat(position.currency()).isEqualTo(EUR);
    }

    @Test
    void keepsTheCurrencyOfAPositionAlreadyHeld() {
        marketData.knows(StubMarketData.instrument("AAPL", "Apple Inc.", "USD"));
        Portfolio portfolio = service.createPortfolio("Depot", EUR);
        service.buy(order(portfolio.id(), "AAPL", "2", "200"));

        Position position = service.buy(order(portfolio.id(), "AAPL", "3", "210"));

        assertThat(position.tranches()).hasSize(2);
        assertThat(position.currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void booksASellAgainstTheOldestTranches() {
        Portfolio portfolio = service.createPortfolio("Depot", EUR);
        service.buy(order(portfolio.id(), "SAP.DE", "10", "100"));

        var closed = service.sell(new SellOrder(portfolio.id(), "SAP.DE", new BigDecimal("4"),
                new BigDecimal("150"), BigDecimal.ZERO, LocalDate.of(2026, 6, 15)));

        assertThat(closed).singleElement()
                .satisfies(trade -> assertThat(trade.gain()).isEqualTo(Money.of("200", "EUR")));
        assertThat(portfolios.findById(portfolio.id()).orElseThrow().positions().getFirst().quantity())
                .isEqualTo(Quantity.of(6));
    }

    @Test
    void leavesTheAggregateUntouchedWhenASaleIsTooBig() {
        Portfolio portfolio = service.createPortfolio("Depot", EUR);
        service.buy(order(portfolio.id(), "SAP.DE", "10", "100"));
        int savesBefore = portfolios.saveCount();

        assertThatExceptionOfType(InsufficientSharesException.class).isThrownBy(() ->
                service.sell(new SellOrder(portfolio.id(), "SAP.DE", new BigDecimal("11"), new BigDecimal("150"),
                        BigDecimal.ZERO, LocalDate.of(2026, 6, 15))));

        assertThat(portfolios.saveCount()).isEqualTo(savesBefore);
        assertThat(portfolios.findById(portfolio.id()).orElseThrow().positions().getFirst().quantity())
                .isEqualTo(Quantity.of(10));
    }

    @Test
    void usesTodayWhenNoTradeDateIsGiven() {
        Portfolio portfolio = service.createPortfolio("Depot", EUR);

        Position position = service.buy(new BuyOrder(portfolio.id(), "SAP.DE", "SAP SE", new BigDecimal("1"),
                new BigDecimal("100"), null, "EUR", null));

        assertThat(position.tranches().getFirst().tradeDate()).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    void complainsAboutAPortfolioThatDoesNotExist() {
        assertThatExceptionOfType(PortfolioNotFoundException.class)
                .isThrownBy(() -> service.buy(order(PortfolioId.generate(), "SAP.DE", "1", "100")));
    }

    @Test
    void renamesAndDeletes() {
        Portfolio portfolio = service.createPortfolio("Alt", EUR);

        service.renamePortfolio(portfolio.id(), "Neu");
        assertThat(portfolios.findById(portfolio.id()).orElseThrow().name()).isEqualTo("Neu");

        service.deletePortfolio(portfolio.id());
        assertThat(portfolios.findById(portfolio.id())).isEmpty();
    }

    private static BuyOrder order(PortfolioId portfolioId, String symbol, String quantity, String price) {
        return new BuyOrder(portfolioId, symbol, symbol, new BigDecimal(quantity), new BigDecimal(price),
                BigDecimal.ZERO, "EUR", LocalDate.of(2026, 1, 15));
    }
}
