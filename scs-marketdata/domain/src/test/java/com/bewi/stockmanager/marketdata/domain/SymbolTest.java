package com.bewi.stockmanager.marketdata.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SymbolTest {

    @ParameterizedTest
    @ValueSource(strings = {"AAPL", "SAP.DE", "^GSPC", "BRK-B", "BTC-EUR", "EURUSD=X"})
    void acceptsTheShapesQuoteFeedsUse(String symbol) {
        assertThat(Symbol.of(symbol).value()).isEqualTo(symbol);
    }

    @Test
    void normalisesCaseAndWhitespace() {
        assertThat(Symbol.of(" sap.de ")).isEqualTo(Symbol.of("SAP.DE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "A B", "AAPL/USD", "THISSYMBOLISWAYTOOLONGFORAT"})
    void rejectsAnythingElse(String symbol) {
        assertThatIllegalArgumentException().isThrownBy(() -> Symbol.of(symbol));
    }
}
