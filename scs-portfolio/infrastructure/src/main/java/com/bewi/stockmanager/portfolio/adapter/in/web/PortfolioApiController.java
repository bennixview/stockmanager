package com.bewi.stockmanager.portfolio.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bewi.stockmanager.portfolio.adapter.in.web.dto.PortfolioView;
import com.bewi.stockmanager.portfolio.adapter.in.web.dto.PositionDetailView;
import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PositionId;

/**
 * The portfolio system's HTTP API.
 *
 * <p>The same use cases the UI drives, for scripts, imports and whatever comes next.
 */
@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolio", description = "Portfolios, positions and trades")
class PortfolioApiController {

    private final ViewPortfolio viewPortfolio;
    private final ManagePortfolios managePortfolios;
    private final TradeInstruments trades;

    PortfolioApiController(ViewPortfolio viewPortfolio, ManagePortfolios managePortfolios, TradeInstruments trades) {
        this.viewPortfolio = viewPortfolio;
        this.managePortfolios = managePortfolios;
        this.trades = trades;
    }

    @Operation(summary = "All portfolios with their current valuation")
    @GetMapping
    List<PortfolioSummary> list() {
        return managePortfolios.listPortfolios().stream()
                .map(portfolio -> new PortfolioSummary(portfolio.id().toString(), portfolio.name(),
                        portfolio.baseCurrency().getCurrencyCode(), portfolio.positions().size()))
                .toList();
    }

    @Operation(summary = "One portfolio, valued at current prices")
    @GetMapping("/{portfolioId}")
    PortfolioView get(@PathVariable String portfolioId) {
        return PortfolioView.from(viewPortfolio.overview(PortfolioId.of(portfolioId)));
    }

    @Operation(summary = "One position with its tranches and closed trades")
    @GetMapping("/{portfolioId}/positions/{positionId}")
    PositionDetailView position(@PathVariable String portfolioId, @PathVariable String positionId) {
        return PositionDetailView.from(
                viewPortfolio.position(PortfolioId.of(portfolioId), PositionId.of(positionId)));
    }

    @Operation(summary = "Create a portfolio")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PortfolioSummary create(@Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = managePortfolios.createPortfolio(request.name(),
                Currency.getInstance(request.currency() == null ? "EUR" : request.currency()));
        return new PortfolioSummary(portfolio.id().toString(), portfolio.name(),
                portfolio.baseCurrency().getCurrencyCode(), 0);
    }

    @Operation(summary = "Buy shares into a portfolio")
    @PostMapping("/{portfolioId}/buy")
    @ResponseStatus(HttpStatus.CREATED)
    PortfolioView buy(@PathVariable String portfolioId, @Valid @RequestBody BuyRequest request) {
        PortfolioId id = PortfolioId.of(portfolioId);
        trades.buy(new TradeInstruments.BuyOrder(id, request.symbol(), request.name(), request.quantity(),
                request.pricePerShare(), request.fee(), request.currency(), request.tradeDate()));
        return PortfolioView.from(viewPortfolio.overview(id));
    }

    @Operation(summary = "Sell shares out of a portfolio")
    @PostMapping("/{portfolioId}/sell")
    PortfolioView sell(@PathVariable String portfolioId, @Valid @RequestBody SellRequest request) {
        PortfolioId id = PortfolioId.of(portfolioId);
        trades.sell(new TradeInstruments.SellOrder(id, request.symbol(), request.quantity(), request.pricePerShare(),
                request.fee(), request.tradeDate()));
        return PortfolioView.from(viewPortfolio.overview(id));
    }

    @Operation(summary = "Delete a portfolio and everything in it")
    @DeleteMapping("/{portfolioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable String portfolioId) {
        managePortfolios.deletePortfolio(PortfolioId.of(portfolioId));
    }

    record PortfolioSummary(String id, String name, String currency, int positionCount) {
    }

    record CreatePortfolioRequest(@NotBlank String name, String currency) {
    }

    record BuyRequest(
            @NotBlank String symbol,
            String name,
            @NotNull @DecimalMin("0.000001") BigDecimal quantity,
            @NotNull @DecimalMin("0.000001") BigDecimal pricePerShare,
            BigDecimal fee,
            String currency,
            LocalDate tradeDate) {
    }

    record SellRequest(
            @NotBlank String symbol,
            @NotNull @DecimalMin("0.000001") BigDecimal quantity,
            @NotNull @DecimalMin("0.000001") BigDecimal pricePerShare,
            BigDecimal fee,
            LocalDate tradeDate) {
    }
}
