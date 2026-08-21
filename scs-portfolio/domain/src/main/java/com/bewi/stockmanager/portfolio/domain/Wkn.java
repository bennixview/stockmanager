package com.bewi.stockmanager.portfolio.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jmolecules.ddd.annotation.ValueObject;

/** German Wertpapierkennnummer - six alphanumeric characters. */
@ValueObject
public record Wkn(String value) {

    private static final Pattern VALID = Pattern.compile("[A-Z0-9]{6}");

    public Wkn {
        Objects.requireNonNull(value, "wkn must not be null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a valid WKN: '" + value + "'");
        }
    }

    public static Wkn of(String value) {
        return new Wkn(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
