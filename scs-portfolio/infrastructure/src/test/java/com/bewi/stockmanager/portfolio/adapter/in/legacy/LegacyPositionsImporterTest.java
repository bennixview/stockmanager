package com.bewi.stockmanager.portfolio.adapter.in.legacy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.bewi.stockmanager.portfolio.application.fixture.InMemoryPortfolioRepository;
import com.bewi.stockmanager.portfolio.application.fixture.StubMarketData;
import com.bewi.stockmanager.portfolio.application.service.PortfolioService;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Isin;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.Quantity;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** The upgrade path: last version's positions.json has to land in the new model without loss. */
class LegacyPositionsImporterTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    private static final String LEGACY_JSON = """
            [
              {
                "id": "e42f9123-ba3e-4377-8244-86a76f39c38f",
                "name": "Amazon",
                "wkn": "906866",
                "tranches": [
                  {"strikeprice": 5, "quantity": 5},
                  {"strikeprice": 100, "quantity": 10, "strikeDate": 1713687776.114090800},
                  {"strikeprice": 0, "quantity": 0}
                ],
                "strikeprice": 105,
                "quantity": 15
              },
              {
                "id": "ee873414-15d6-4bcb-9939-f8641ee61a16",
                "name": "Ein Unbekanntes Papier",
                "wkn": "999999",
                "tranches": [{"strikeprice": 20, "quantity": 3, "strikeDate": 1713687776.114090800}]
              }
            ]
            """;

    @TempDir
    private Path directory;

    private InMemoryPortfolioRepository portfolios;
    private PortfolioService service;
    private LegacyPositionsImporter importer;

    @BeforeEach
    void setUp() {
        portfolios = new InMemoryPortfolioRepository();
        StubMarketData marketData = new StubMarketData().knows(new InstrumentRef(Symbol.of("AMZN"),
                "Amazon.com Inc.", Isin.of("US0231351067"), Wkn.of("906866"), Currency.getInstance("USD")));
        service = new PortfolioService(portfolios, marketData, EUR, CLOCK);
        importer = new LegacyPositionsImporter(service, service, marketData, ZoneOffset.UTC);
    }

    @Test
    void resolvesWknsAgainstTheMarketDataCatalogue() throws IOException {
        Portfolio target = service.createPortfolio("Depot", EUR);

        importer.importFrom(write(LEGACY_JSON), target);

        Portfolio reloaded = portfolios.findById(target.id()).orElseThrow();
        assertThat(reloaded.findPosition(Symbol.of("AMZN"))).hasValueSatisfying(position -> {
            assertThat(position.instrument().name()).isEqualTo("Amazon.com Inc.");
            assertThat(position.currency().getCurrencyCode()).isEqualTo("USD");
            assertThat(position.quantity()).isEqualTo(Quantity.of(15));
        });
    }

    @Test
    void turnsEveryLegacyTrancheIntoATranche() throws IOException {
        Portfolio target = service.createPortfolio("Depot", EUR);

        int booked = importer.importFrom(write(LEGACY_JSON), target);

        assertThat(booked).isEqualTo(3);
        Position amazon = portfolios.findById(target.id()).orElseThrow()
                .findPosition(Symbol.of("AMZN")).orElseThrow();
        assertThat(amazon.tranches()).hasSize(2);
        assertThat(amazon.invested()).isEqualTo(Money.of("1025", "USD"));
    }

    @Test
    // A tranche without a date in the old file gets today from the use case, which owns the clock.
    void keepsTheOriginalTradeDates() throws IOException {
        Portfolio target = service.createPortfolio("Depot", EUR);

        importer.importFrom(write(LEGACY_JSON), target);

        Position amazon = portfolios.findById(target.id()).orElseThrow()
                .findPosition(Symbol.of("AMZN")).orElseThrow();
        assertThat(amazon.tranches()).extracting(tranche -> tranche.tradeDate())
                .contains(LocalDate.of(2024, 4, 21), LocalDate.of(2026, 6, 15));
    }

    @Test
    void skipsTheEmptyTranchesTheOldModelAllowed() throws IOException {
        Portfolio target = service.createPortfolio("Depot", EUR);

        importer.importFrom(write(LEGACY_JSON), target);

        assertThat(portfolios.findById(target.id()).orElseThrow().findPosition(Symbol.of("AMZN")).orElseThrow()
                .tranches()).noneMatch(tranche -> tranche.quantity().isZero());
    }

    @Test
    void importsWhatItCannotResolveUnderItsWknRatherThanDroppingIt() throws IOException {
        Portfolio target = service.createPortfolio("Depot", EUR);

        importer.importFrom(write(LEGACY_JSON), target);

        assertThat(portfolios.findById(target.id()).orElseThrow().findPosition(Symbol.of("999999")))
                .hasValueSatisfying(position -> {
                    assertThat(position.instrument().name()).isEqualTo("Ein Unbekanntes Papier");
                    assertThat(position.quantity()).isEqualTo(Quantity.of(3));
                    assertThat(position.currency()).isEqualTo(EUR);
                });
    }

    @Test
    void importsIntoTheDefaultPortfolioWhenNoneIsGiven() throws IOException {
        importer.importFrom(write(LEGACY_JSON));

        assertThat(portfolios.count()).isEqualTo(1);
        assertThat(portfolios.findFirst().orElseThrow().positions()).hasSize(2);
    }

    @Test
    void handlesAnEmptyFile() throws IOException {
        assertThat(importer.importFrom(write("[]"))).isZero();
    }

    @Test
    void failsLoudlyOnAFileItCannotRead() throws IOException {
        Path broken = write("{not json");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> importer.importFrom(broken))
                .withMessageContaining("Could not read legacy positions");
    }

    @Test
    void importsTheRepositorysOwnPositionsFile() throws IOException {
        Path shipped = Path.of("..", "..", "positions.json").toAbsolutePath().normalize();
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.isReadable(shipped),
                "positions.json of the previous version is not present");

        Portfolio target = service.createPortfolio("Depot", EUR);
        int booked = importer.importFrom(shipped, target);

        assertThat(booked).isPositive();
        assertThat(portfolios.findById(target.id()).orElseThrow().positions()).isNotEmpty();
    }

    private Path write(String content) throws IOException {
        Path file = directory.resolve("positions.json");
        Files.writeString(file, content);
        return file;
    }
}
