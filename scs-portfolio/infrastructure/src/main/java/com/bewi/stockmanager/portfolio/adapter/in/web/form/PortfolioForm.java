package com.bewi.stockmanager.portfolio.adapter.in.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creating or renaming a portfolio. */
public class PortfolioForm {

    @NotBlank(message = "A portfolio needs a name")
    @Size(max = 100, message = "That name is too long")
    private String name;

    @Pattern(regexp = "[A-Z]{3}", message = "Use a three-letter currency code such as EUR")
    private String currency = "EUR";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
