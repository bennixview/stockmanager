package com.bewi.stockmanager.marketdata.adapter.out.feed.yahoo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
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
 * Live prices from Yahoo Finance's public chart endpoint.
 *
 * <p>One endpoint serves both the last price and the candles, which keeps this adapter small; it
 * needs no API key but is a courtesy service, so failures are expected and reported as empty
 * results rather than exceptions.
 */
public class YahooFinanceQuoteFeed implements MarketDataFeed {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceQuoteFeed.class);

    private final RestClient restClient;

    public YahooFinanceQuoteFeed(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String name() {
        return "yahoo";
    }

    @Override
    public Optional<Quote> latestQuote(Instrument instrument) {
        return chart(instrument, "1d", "5m").flatMap(result -> toQuote(instrument, result));
    }

    @Override
    public PriceHistory history(Instrument instrument, HistoryRange range) {
        YahooRange yahooRange = YahooRange.of(range);
        return chart(instrument, yahooRange.range(), yahooRange.interval())
                .map(result -> toHistory(instrument, range, result))
                .orElseGet(() -> PriceHistory.empty(instrument.symbol(), range));
    }

    private Optional<JsonNode> chart(Instrument instrument, String range, String interval) {
        try {
            JsonNode response = restClient.get()
                    .uri("/v8/finance/chart/{symbol}?range={range}&interval={interval}",
                            instrument.symbol().value(), range, interval)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                return Optional.empty();
            }
            JsonNode results = response.path("chart").path("result");
            if (!results.isArray() || results.isEmpty()) {
                log.debug("Yahoo returned no result for {}: {}", instrument.symbol(),
                        response.path("chart").path("error").asString("no error given"));
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (RuntimeException e) {
            log.warn("Yahoo chart request for {} failed: {}", instrument.symbol(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Quote> toQuote(Instrument instrument, JsonNode result) {
        JsonNode meta = result.path("meta");
        BigDecimal price = decimal(meta, "regularMarketPrice");
        if (price == null) {
            return Optional.empty();
        }
        BigDecimal previousClose = Optional.ofNullable(decimal(meta, "chartPreviousClose"))
                .orElseGet(() -> Optional.ofNullable(decimal(meta, "previousClose")).orElse(price));
        Quotation quotation = quotationOf(meta, instrument);
        Instant asOf = meta.hasNonNull("regularMarketTime")
                ? Instant.ofEpochSecond(meta.get("regularMarketTime").asLong())
                : Instant.now();
        return Optional.of(new Quote(
                instrument.symbol(),
                quotation.toMoney(price),
                quotation.toMoney(previousClose),
                marketState(meta.path("marketState").asString(null)),
                asOf));
    }

    private PriceHistory toHistory(Instrument instrument, HistoryRange range, JsonNode result) {
        JsonNode timestamps = result.path("timestamp");
        JsonNode quote = result.path("indicators").path("quote");
        if (!timestamps.isArray() || !quote.isArray() || quote.isEmpty()) {
            return PriceHistory.empty(instrument.symbol(), range);
        }
        JsonNode ohlc = quote.get(0);
        List<Candle> candles = new ArrayList<>(timestamps.size());
        for (int i = 0; i < timestamps.size(); i++) {
            BigDecimal close = decimalAt(ohlc.path("close"), i);
            if (close == null) {
                continue; // Yahoo pads gaps with nulls
            }
            candles.add(new Candle(
                    Instant.ofEpochSecond(timestamps.get(i).asLong()),
                    decimalAt(ohlc.path("open"), i),
                    decimalAt(ohlc.path("high"), i),
                    decimalAt(ohlc.path("low"), i),
                    close,
                    ohlc.path("volume").path(i).asLong(0)));
        }
        return new PriceHistory(instrument.symbol(), range, candles);
    }

    /**
     * How to read the numbers Yahoo sends.
     *
     * <p>Some venues are quoted in minor units - London in pence, Johannesburg in cents - and Yahoo
     * signals that with a lower-case last letter in the currency code. Reading {@code GBp} as
     * {@code GBP} would overstate a British holding by a factor of a hundred, so the divisor is part
     * of the answer.
     */
    private record Quotation(Currency currency, BigDecimal divisor) {

        Money toMoney(BigDecimal amount) {
            BigDecimal value = divisor.compareTo(BigDecimal.ONE) == 0
                    ? amount
                    : amount.divide(divisor, 6, java.math.RoundingMode.HALF_UP);
            return Money.of(value, currency);
        }
    }

    private static final Map<String, Quotation> MINOR_UNITS = Map.of(
            "GBp", new Quotation(Currency.getInstance("GBP"), BigDecimal.valueOf(100)),
            "ZAc", new Quotation(Currency.getInstance("ZAR"), BigDecimal.valueOf(100)),
            "ILA", new Quotation(Currency.getInstance("ILS"), BigDecimal.valueOf(100)));

    private static Quotation quotationOf(JsonNode meta, Instrument instrument) {
        String code = meta.path("currency").asString(null);
        if (code == null || code.isBlank()) {
            return new Quotation(instrument.currency(), BigDecimal.ONE);
        }
        Quotation minorUnit = MINOR_UNITS.get(code);
        if (minorUnit != null) {
            return minorUnit;
        }
        try {
            return new Quotation(Currency.getInstance(code.toUpperCase(Locale.ROOT)), BigDecimal.ONE);
        } catch (IllegalArgumentException e) {
            // Nothing we can map; the catalogue's currency is the better guess.
            return new Quotation(instrument.currency(), BigDecimal.ONE);
        }
    }

    private static MarketState marketState(String state) {
        if (state == null) {
            return MarketState.UNKNOWN;
        }
        return switch (state.toUpperCase(Locale.ROOT)) {
            case "REGULAR" -> MarketState.OPEN;
            case "PRE", "PREPRE" -> MarketState.PRE_MARKET;
            case "POST", "POSTPOST" -> MarketState.POST_MARKET;
            case "CLOSED" -> MarketState.CLOSED;
            default -> MarketState.UNKNOWN;
        };
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }

    private static BigDecimal decimalAt(JsonNode array, int index) {
        JsonNode value = array.path(index);
        return value.isNumber() ? value.decimalValue() : null;
    }

    /** Yahoo's own vocabulary for the ranges the domain knows. */
    private record YahooRange(String range, String interval) {

        static YahooRange of(HistoryRange range) {
            return switch (range) {
                case DAY -> new YahooRange("1d", "5m");
                case WEEK -> new YahooRange("5d", "60m");
                case MONTH -> new YahooRange("1mo", "1d");
                case QUARTER -> new YahooRange("3mo", "1d");
                case YEAR -> new YahooRange("1y", "1d");
                case FIVE_YEARS -> new YahooRange("5y", "1wk");
            };
        }
    }
}
