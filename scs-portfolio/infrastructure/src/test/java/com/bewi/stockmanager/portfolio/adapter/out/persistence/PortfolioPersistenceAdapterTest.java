package com.bewi.stockmanager.portfolio.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Isin;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

import static org.assertj.core.api.Assertions.assertThat;

/** The aggregate has to survive a round trip through the database unchanged. */
@DataJpaTest
@Import(PortfolioPersistenceAdapter.class)
class PortfolioPersistenceAdapterTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDate JANUARY = LocalDate.of(2026, 1, 15);
    private static final LocalDate JUNE = LocalDate.of(2026, 6, 15);

    @Autowired
    private PortfolioRepository repository;

    @Test
    void storesAndReloadsAWholeAggregate() {
        Portfolio portfolio = Portfolio.open("Mein Depot", EUR, Instant.parse("2026-01-01T00:00:00Z"));
        portfolio.buy(sap(), Quantity.of(10), Money.of("100", "EUR"), Money.of("9.90", "EUR"), JANUARY);
        portfolio.buy(sap(), Quantity.of("2.5"), Money.of("120", "EUR"), Money.of("1", "EUR"), JUNE);
        repository.save(portfolio);

        Portfolio reloaded = repository.findById(portfolio.id()).orElseThrow();

        assertThat(reloaded.name()).isEqualTo("Mein Depot");
        assertThat(reloaded.baseCurrency()).isEqualTo(EUR);
        Position position = reloaded.positions().getFirst();
        assertThat(position.id()).isEqualTo(portfolio.positions().getFirst().id());
        assertThat(position.quantity()).isEqualTo(Quantity.of("12.5"));
        assertThat(position.invested()).isEqualTo(Money.of("1310.90", "EUR"));
        assertThat(position.instrument().isinValue()).contains(Isin.of("DE0007164600"));
        assertThat(position.instrument().wknValue()).contains(Wkn.of("716460"));
    }

    @Test
    void keepsRealizedTradesAcrossAReload() {
        Portfolio portfolio = Portfolio.open("Depot", EUR, Instant.now());
        portfolio.buy(sap(), Quantity.of(10), Money.of("100", "EUR"), null, JANUARY);
        portfolio.sell(Symbol.of("SAP.DE"), Quantity.of(4), Money.of("150", "EUR"), null, JUNE);
        repository.save(portfolio);

        Portfolio reloaded = repository.findById(portfolio.id()).orElseThrow();

        Position position = reloaded.positions().getFirst();
        assertThat(position.quantity()).isEqualTo(Quantity.of(6));
        assertThat(position.realizedTrades()).hasSize(1);
        assertThat(position.realizedGain()).isEqualTo(Money.of("200", "EUR"));
    }

    @Test
    void replacesTranchesRatherThanDuplicatingThemOnEverySave() {
        Portfolio portfolio = Portfolio.open("Depot", EUR, Instant.now());
        portfolio.buy(sap(), Quantity.of(10), Money.of("100", "EUR"), null, JANUARY);
        repository.save(portfolio);
        repository.save(portfolio);
        repository.save(portfolio);

        assertThat(repository.findById(portfolio.id()).orElseThrow().positions().getFirst().tranches()).hasSize(1);
    }

    @Test
    void keepsForeignCurrencyPositionsInTheirOwnCurrency() {
        Portfolio portfolio = Portfolio.open("Depot", EUR, Instant.now());
        portfolio.buy(InstrumentRef.of(Symbol.of("AAPL"), "Apple Inc.", USD), Quantity.of(3),
                Money.of("200", "USD"), null, JANUARY);
        repository.save(portfolio);

        assertThat(repository.findById(portfolio.id()).orElseThrow().positions().getFirst().currency())
                .isEqualTo(USD);
    }

    @Test
    void dropsAPositionThatWasRemovedFromTheAggregate() {
        Portfolio portfolio = Portfolio.open("Depot", EUR, Instant.now());
        Position position = portfolio.buy(sap(), Quantity.of(10), Money.of("100", "EUR"), null, JANUARY);
        repository.save(portfolio);

        portfolio.removePosition(position.id());
        repository.save(portfolio);

        assertThat(repository.findById(portfolio.id()).orElseThrow().allPositions()).isEmpty();
    }

    @Test
    void listsPortfoliosInTheOrderTheyWereCreated() {
        repository.save(Portfolio.open("Erstes", EUR, Instant.parse("2026-01-01T00:00:00Z")));
        repository.save(Portfolio.open("Zweites", EUR, Instant.parse("2026-02-01T00:00:00Z")));

        assertThat(repository.findAll()).extracting(Portfolio::name).containsExactly("Erstes", "Zweites");
        assertThat(repository.findFirst()).hasValueSatisfying(
                portfolio -> assertThat(portfolio.name()).isEqualTo("Erstes"));
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void deletesAPortfolioWithEverythingInIt() {
        Portfolio portfolio = Portfolio.open("Depot", EUR, Instant.now());
        portfolio.buy(sap(), Quantity.of(10), Money.of("100", "EUR"), null, JANUARY);
        repository.save(portfolio);

        repository.delete(portfolio.id());

        assertThat(repository.findById(portfolio.id())).isEmpty();
    }

    @Test
    void reportsNothingForAnUnknownPortfolio() {
        assertThat(repository.findById(PortfolioId.generate())).isEmpty();
        assertThat(repository.findAll()).isEqualTo(List.of());
    }

    private static InstrumentRef sap() {
        return new InstrumentRef(Symbol.of("SAP.DE"), "SAP SE", Isin.of("DE0007164600"), Wkn.of("716460"), EUR);
    }
}
