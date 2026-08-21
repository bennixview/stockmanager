package com.bewi.stockmanager.portfolio.adapter.out.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Isin;
import com.bewi.stockmanager.portfolio.domain.Money;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/**
 * Driven adapter to the market data system, over its public HTTP API.
 *
 * <p>Two self-contained systems integrate over the network, never over a shared database or a
 * shared library, so this class owns its own DTOs for the other system's contract and translates
 * them into this system's language.
 *
 * <p>Every call is best effort: a market data outage degrades the portfolio to "no prices", it does
 * not break it.
 */
public class MarketDataRestAdapter implements MarketDataPort {

    private static final Logger log = LoggerFactory.getLogger(MarketDataRestAdapter.class);
    private static final ParameterizedTypeReference<List<QuoteDto>> QUOTE_LIST = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<InstrumentDto>> INSTRUMENT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public MarketDataRestAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<MarketPrice> priceFor(Symbol symbol) {
        return get("/api/quotes/{symbol}", QuoteDto.class, symbol.value()).map(QuoteDto::toMarketPrice);
    }

    @Override
    public Map<Symbol, MarketPrice> pricesFor(Collection<Symbol> symbols) {
        if (symbols.isEmpty()) {
            return Map.of();
        }
        String joined = symbols.stream().map(Symbol::value).distinct().reduce((a, b) -> a + "," + b).orElseThrow();
        List<QuoteDto> quotes = getList("/api/quotes?symbols={symbols}", QUOTE_LIST, joined);
        Map<Symbol, MarketPrice> prices = new HashMap<>(quotes.size());
        for (QuoteDto quote : quotes) {
            MarketPrice price = quote.toMarketPrice();
            prices.put(price.symbol(), price);
        }
        return Map.copyOf(prices);
    }

    @Override
    public List<InstrumentRef> search(String query, int limit) {
        return getList("/api/instruments?query={query}&limit={limit}", INSTRUMENT_LIST, query, String.valueOf(limit))
                .stream()
                .map(InstrumentDto::toInstrumentRef)
                .toList();
    }

    @Override
    public Optional<InstrumentRef> bySymbol(Symbol symbol) {
        return get("/api/instruments/{symbol}", InstrumentDto.class, symbol.value())
                .map(InstrumentDto::toInstrumentRef);
    }

    @Override
    public Optional<InstrumentRef> byWkn(Wkn wkn) {
        return get("/api/instruments/by-wkn/{wkn}", InstrumentDto.class, wkn.value())
                .map(InstrumentDto::toInstrumentRef);
    }

    private <T> Optional<T> get(String uriTemplate, Class<T> type, Object... uriVariables) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(uriTemplate, uriVariables)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        // Not knowing an instrument is an answer, not a failure.
                    })
                    .body(type));
        } catch (RuntimeException e) {
            log.warn("Market data system unreachable for {}: {}", uriTemplate, e.getMessage());
            return Optional.empty();
        }
    }

    private <T> List<T> getList(String uriTemplate, ParameterizedTypeReference<List<T>> type, Object... uriVariables) {
        try {
            List<T> body = restClient.get().uri(uriTemplate, uriVariables).retrieve().body(type);
            return body == null ? List.of() : body;
        } catch (RuntimeException e) {
            log.warn("Market data system unreachable for {}: {}", uriTemplate, e.getMessage());
            return List.of();
        }
    }

    /** The market data system's quote representation - its contract, mirrored here. */
    record QuoteDto(String symbol, BigDecimal price, String currency, BigDecimal previousClose, BigDecimal change,
            BigDecimal changePercent, String marketState, Instant asOf) {

        MarketPrice toMarketPrice() {
            return new MarketPrice(Symbol.of(symbol), Money.of(price, Currency.getInstance(currency)), changePercent,
                    asOf);
        }
    }

    /** The market data system's instrument representation. */
    record InstrumentDto(String symbol, String name, String isin, String wkn, String currency, String exchange,
            String type) {

        InstrumentRef toInstrumentRef() {
            return new InstrumentRef(
                    Symbol.of(symbol),
                    name,
                    isin == null || isin.isBlank() ? null : Isin.of(isin),
                    wkn == null || wkn.isBlank() ? null : Wkn.of(wkn),
                    Currency.getInstance(currency == null || currency.isBlank() ? "EUR" : currency));
        }
    }
}
