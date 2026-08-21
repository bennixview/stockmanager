package com.bewi.stockmanager.marketdata.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class IsinTest {

    @ParameterizedTest
    @ValueSource(strings = {"US0378331005", "DE0007164600", "IE00B4L5Y983", "US88160R1014"})
    void acceptsRealIsins(String isin) {
        assertThat(Isin.of(isin).value()).isEqualTo(isin);
    }

    @Test
    void normalisesCaseAndWhitespace() {
        assertThat(Isin.of(" us0378331005 ").value()).isEqualTo("US0378331005");
    }

    @Test
    void rejectsAWrongCheckDigit() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Isin.of("US0378331006"))
                .withMessageContaining("check digit");
    }

    @ParameterizedTest
    @ValueSource(strings = {"US037833100", "0S0378331005", "US03783310051"})
    void rejectsMalformedInput(String isin) {
        assertThatIllegalArgumentException().isThrownBy(() -> Isin.of(isin));
    }
}
