package com.bewi.stockmanager.marketdata.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jmolecules.ddd.annotation.ValueObject;

/** International Securities Identification Number, validated including its check digit. */
@ValueObject
public record Isin(String value) {

    private static final Pattern SHAPE = Pattern.compile("[A-Z]{2}[A-Z0-9]{9}[0-9]");

    public Isin {
        Objects.requireNonNull(value, "isin must not be null");
        value = value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (!SHAPE.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a well-formed ISIN: '" + value + "'");
        }
        if (!hasValidCheckDigit(value)) {
            throw new IllegalArgumentException("ISIN check digit is wrong: '" + value + "'");
        }
    }

    public static Isin of(String value) {
        return new Isin(value);
    }

    /** Luhn check over the ISIN with letters expanded to their two-digit ordinal (A=10 ... Z=35). */
    private static boolean hasValidCheckDigit(String isin) {
        StringBuilder digits = new StringBuilder(24);
        for (char c : isin.toCharArray()) {
            digits.append(Character.isDigit(c) ? String.valueOf(c) : String.valueOf(c - 'A' + 10));
        }
        int sum = 0;
        boolean doubling = true;
        for (int i = digits.length() - 2; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubling) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubling = !doubling;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == digits.charAt(digits.length() - 1) - '0';
    }

    @Override
    public String toString() {
        return value;
    }
}
