package com.bewi.stockmanager.marketdata.adapter.in.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bewi.stockmanager.marketdata.adapter.in.web.dto.PriceHistoryResponse;
import com.bewi.stockmanager.marketdata.adapter.in.web.dto.QuoteResponse;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteQuery;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Driving adapter: the HTTP face of {@link QuoteQuery}.
 *
 * <p>This is the contract other self-contained systems depend on, so it speaks its own DTOs and
 * never leaks domain types.
 */
@RestController
@RequestMapping("/api/quotes")
@Tag(name = "Quotes", description = "Live prices and price history")
class QuoteApiController {

    private final QuoteQuery quotes;

    QuoteApiController(QuoteQuery quotes) {
        this.quotes = quotes;
    }

    @Operation(summary = "Latest quotes for a comma-separated list of symbols")
    @GetMapping
    List<QuoteResponse> quotes(@RequestParam("symbols") List<String> symbols) {
        Map<Symbol, com.bewi.stockmanager.marketdata.domain.Quote> found = quotes.quotesFor(
                symbols.stream().map(Symbol::of).collect(Collectors.toSet()));
        return found.values().stream()
                .map(QuoteResponse::from)
                .sorted(java.util.Comparator.comparing(QuoteResponse::symbol))
                .toList();
    }

    @Operation(summary = "Latest quote for a single symbol")
    @GetMapping("/{symbol}")
    ResponseEntity<QuoteResponse> quote(@PathVariable String symbol) {
        return quotes.quoteFor(Symbol.of(symbol))
                .map(QuoteResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Price history for a symbol", description = "Ranges: 1d, 1w, 1m, 3m, 1y, 5y")
    @GetMapping("/{symbol}/history")
    PriceHistoryResponse history(@PathVariable String symbol,
            @RequestParam(value = "range", defaultValue = "1m") String range) {
        return PriceHistoryResponse.from(quotes.historyFor(Symbol.of(symbol), HistoryRange.fromCode(range)));
    }
}
