package com.bewi.stockmanager.marketdata.adapter.out.feed.alphavantage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import com.bewi.stockmanager.marketdata.adapter.out.feed.MarketDataFeed;
import com.bewi.stockmanager.marketdata.domain.Candle;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.MarketState;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;

/**
 * Live prices from Alpha Vantage.
 *
 * <p>Needs an API key and is rate limited hard on the free tier, so it suits a small watch list
 * with a slow refresh interval rather than a busy dashboard.
 */
public class AlphaVantageQuoteFeed implements MarketDataFeed {

    private static final Logger log = LoggerFactory.getLogger(AlphaVantageQuoteFeed.class);
    private static final String RATE_LIMIT_FIELD = "Note";

    private final RestClient restClient;
    private final String apiKey;

    public AlphaVantageQuoteFeed(RestClient restClient, String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public String name() {
        return "alphavantage";
    }

    @Override
    public Optional<Quote> latestQuote(Instrument instrument) {
        return call("GLOBAL_QUOTE", instrument, Map.of()).flatMap(body -> {
            JsonNode quote = body.path("Global Quote");
            BigDecimal price = decimal(quote, "05. price");
            if (price == null) {
                log.debug("Alpha Vantage returned no price for {}: {}", instrument.symbol(), body);
                return Optional.empty();
            }
            BigDecimal previousClose = Optional.ofNullable(decimal(quote, "08. previous close")).orElse(price);
            return Optional.of(new Quote(
                    instrument.symbol(),
                    Money.of(price, instrument.currency()),
                    Money.of(previousClose, instrument.currency()),
                    MarketState.UNKNOWN,
                    Instant.now()));
        });
    }

    @Override
    public PriceHistory history(Instrument instrument, HistoryRange range) {
        Optional<JsonNode> body = call("TIME_SERIES_DAILY", instrument, Map.of("outputsize",
                range.period().toTotalMonths() > 3 ? "full" : "compact"));
        if (body.isEmpty()) {
            return PriceHistory.empty(instrument.symbol(), range);
        }
        JsonNode series = body.get().path("Time Series (Daily)");
        LocalDate from = LocalDate.now(ZoneOffset.UTC).minus(range.period());
        List<Candle> candles = new ArrayList<>();
        for (Map.Entry<String, JsonNode> day : series.properties()) {
            LocalDate date = LocalDate.parse(day.getKey());
            if (date.isBefore(from)) {
                continue;
            }
            JsonNode ohlc = day.getValue();
            BigDecimal close = decimal(ohlc, "4. close");
            if (close == null) {
                continue;
            }
            candles.add(new Candle(date.atStartOfDay(ZoneOffset.UTC).toInstant(),
                    decimal(ohlc, "1. open"), decimal(ohlc, "2. high"), decimal(ohlc, "3. low"), close,
                    ohlc.path("5. volume").asLong(0)));
        }
        return new PriceHistory(instrument.symbol(), range, candles);
    }

    private Optional<JsonNode> call(String function, Instrument instrument, Map<String, String> extraParams) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No Alpha Vantage API key configured; cannot price {}", instrument.symbol());
            return Optional.empty();
        }
        try {
            JsonNode body = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/query")
                                .queryParam("function", function)
                                .queryParam("symbol", instrument.symbol().value())
                                .queryParam("apikey", apiKey);
                        extraParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null) {
                return Optional.empty();
            }
            if (body.hasNonNull(RATE_LIMIT_FIELD) || body.hasNonNull("Information")) {
                log.warn("Alpha Vantage rate limit hit: {}",
                        body.path(RATE_LIMIT_FIELD).asString(body.path("Information").asString("")));
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (RuntimeException e) {
            log.warn("Alpha Vantage {} request for {} failed: {}", function, instrument.symbol(), e.getMessage());
            return Optional.empty();
        }
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        String value = node.path(field).asString(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
