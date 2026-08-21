package com.bewi.stockmanager.marketdata.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bewi.stockmanager.marketdata.adapter.in.web.dto.InstrumentResponse;
import com.bewi.stockmanager.marketdata.adapter.in.web.dto.RegisterInstrumentRequest;
import com.bewi.stockmanager.marketdata.application.port.in.InstrumentCatalog;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Driving adapter for the instrument catalogue. */
@RestController
@RequestMapping("/api/instruments")
@Tag(name = "Instruments", description = "The catalogue of instruments this system can price")
class InstrumentApiController {

    private final InstrumentCatalog catalog;

    InstrumentApiController(InstrumentCatalog catalog) {
        this.catalog = catalog;
    }

    @Operation(summary = "Search instruments by symbol, name, ISIN or WKN")
    @GetMapping
    List<InstrumentResponse> search(@RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return catalog.search(query, limit).stream().map(InstrumentResponse::from).toList();
    }

    @Operation(summary = "One instrument by its ticker symbol")
    @GetMapping("/{symbol}")
    ResponseEntity<InstrumentResponse> bySymbol(@PathVariable String symbol) {
        return catalog.bySymbol(Symbol.of(symbol))
                .map(InstrumentResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "One instrument by WKN", description = "Used by German brokers and by the portfolio system's import")
    @GetMapping("/by-wkn/{wkn}")
    ResponseEntity<InstrumentResponse> byWkn(@PathVariable String wkn) {
        return catalog.byWkn(Wkn.of(wkn))
                .map(InstrumentResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "One instrument by ISIN")
    @GetMapping("/by-isin/{isin}")
    ResponseEntity<InstrumentResponse> byIsin(@PathVariable String isin) {
        return catalog.byIsin(Isin.of(isin))
                .map(InstrumentResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Add an instrument to the catalogue")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    InstrumentResponse register(@Valid @RequestBody RegisterInstrumentRequest request) {
        return InstrumentResponse.from(catalog.register(request.toDomain()));
    }
}
