package com.bewi.stockmanager.portfolio.adapter.in.web.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import org.springframework.format.annotation.DateTimeFormat;

/** The sell form. */
public class SellForm {

    @NotBlank
    private String symbol;

    @NotNull(message = "How many shares?")
    @DecimalMin(value = "0.000001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "At what price?")
    @DecimalMin(value = "0.000001", message = "Price must be greater than zero")
    private BigDecimal pricePerShare;

    @DecimalMin(value = "0.0", message = "A fee cannot be negative")
    private BigDecimal fee = BigDecimal.ZERO;

    @NotNull(message = "When was the trade?")
    @PastOrPresent(message = "A trade cannot be booked for the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tradeDate = LocalDate.now();

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
}
