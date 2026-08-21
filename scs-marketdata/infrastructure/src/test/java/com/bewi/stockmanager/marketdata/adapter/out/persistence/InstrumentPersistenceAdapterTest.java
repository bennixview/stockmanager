package com.bewi.stockmanager.marketdata.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;
import com.bewi.stockmanager.marketdata.domain.InstrumentType;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the repository port against a real database, including the Flyway schema and the seed
 * the portfolio import relies on.
 */
@DataJpaTest
@Import(InstrumentPersistenceAdapter.class)
class InstrumentPersistenceAdapterTest {

    @Autowired
    private InstrumentRepository repository;

    @Test
    void storesAndReadsBackEveryField() {
        Instrument saved = repository.save(Instrument.builder(Symbol.of("TEST.DE"))
                .name("Test AG")
                .isin(Isin.of("DE0007164600"))
                .wkn(Wkn.of("716461"))
                .currency("EUR")
                .exchange("XETRA")
                .type(InstrumentType.STOCK)
                .build());

        assertThat(repository.findBySymbol(saved.symbol())).hasValueSatisfying(instrument -> {
            assertThat(instrument.name()).isEqualTo("Test AG");
            assertThat(instrument.isin()).contains(Isin.of("DE0007164600"));
            assertThat(instrument.wkn()).contains(Wkn.of("716461"));
            assertThat(instrument.exchange()).contains("XETRA");
            assertThat(instrument.type()).isEqualTo(InstrumentType.STOCK);
        });
    }

    @Test
    void findsSeededInstrumentsByAlternativeKeys() {
        assertThat(repository.findByWkn(Wkn.of("906866"))).hasValueSatisfying(
                instrument -> assertThat(instrument.symbol()).isEqualTo(Symbol.of("AMZN")));
        assertThat(repository.findByIsin(Isin.of("DE0007164600"))).hasValueSatisfying(
                instrument -> assertThat(instrument.symbol()).isEqualTo(Symbol.of("SAP.DE")));
    }

    @Test
    void searchesOverSymbolNameIsinAndWkn() {
        assertThat(repository.search("apple", 10)).extracting(instrument -> instrument.symbol().value())
                .containsExactly("AAPL");
        assertThat(repository.search("865985", 10)).hasSize(1);
        assertThat(repository.search("IE00B4L5Y983", 10)).hasSize(1);
    }

    @Test
    void ranksAnExactSymbolMatchFirst() {
        assertThat(repository.search("SAP.DE", 10)).first()
                .extracting(instrument -> instrument.symbol().value())
                .isEqualTo("SAP.DE");
    }

    @Test
    void honoursTheSearchLimit() {
        assertThat(repository.search("e", 3)).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void deletesBySymbol() {
        repository.save(Instrument.builder(Symbol.of("GONE")).name("Gone").build());
        repository.delete(Symbol.of("GONE"));

        assertThat(repository.findBySymbol(Symbol.of("GONE"))).isEmpty();
    }
}
