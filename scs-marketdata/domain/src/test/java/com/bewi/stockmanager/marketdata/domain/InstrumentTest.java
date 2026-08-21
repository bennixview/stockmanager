package com.bewi.stockmanager.marketdata.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentTest {

    private final Instrument apple = Instrument.builder(Symbol.of("AAPL"))
            .name("Apple Inc.")
            .isin(Isin.of("US0378331005"))
            .wkn(Wkn.of("865985"))
            .currency("USD")
            .exchange("NASDAQ")
            .type(InstrumentType.STOCK)
            .build();

    @Test
    void matchesOnEveryIdentifierItCarries() {
        assertThat(apple.matches("aapl")).isTrue();
        assertThat(apple.matches("apple")).isTrue();
        assertThat(apple.matches("US03783")).isTrue();
        assertThat(apple.matches("865985")).isTrue();
        assertThat(apple.matches("microsoft")).isFalse();
    }

    @Test
    void matchesEverythingOnAnEmptyQuery() {
        assertThat(apple.matches("")).isTrue();
        assertThat(apple.matches(null)).isTrue();
    }

    @Test
    void isIdentifiedByItsSymbol() {
        Instrument sameSymbolDifferentName = Instrument.builder(Symbol.of("AAPL")).name("Apple").build();

        assertThat(apple).isEqualTo(sameSymbolDifferentName);
    }

    @Test
    void hasOptionalAlternativeKeys() {
        Instrument index = Instrument.builder(Symbol.of("^GSPC")).name("S&P 500").currency("USD").build();

        assertThat(index.isin()).isEmpty();
        assertThat(index.wkn()).isEmpty();
        assertThat(index.type()).isEqualTo(InstrumentType.OTHER);
    }
}
