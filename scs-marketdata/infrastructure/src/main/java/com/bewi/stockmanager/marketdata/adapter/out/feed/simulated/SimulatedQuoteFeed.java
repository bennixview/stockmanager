package com.bewi.stockmanager.marketdata.adapter.out.feed.simulated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bewi.stockmanager.marketdata.adapter.out.feed.MarketDataFeed;
import com.bewi.stockmanager.marketdata.domain.Candle;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.MarketState;
import com.bewi.stockmanager.marketdata.domain.Money;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Quote;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * A feed that makes prices up.
 *
 * <p>It is the default so the application runs, and demos, without an API key or internet access.
 * Prices are a random walk anchored to a value derived from the symbol, so the same symbol always
 * starts in the same region and the numbers stay recognisable across restarts.
 */
public class SimulatedQuoteFeed implements MarketDataFeed {

    private static final BigDecimal MAX_STEP_PERCENT = new BigDecimal("0.35");

    /** Currency pairs in the ticker convention the feeds use, e.g. {@code USDEUR=X}. */
    private static final Pattern CURRENCY_PAIR = Pattern.compile("([A-Z]{3})([A-Z]{3})=X");

    private final Clock clock;
    private final Map<Symbol, BigDecimal> lastPrice = new ConcurrentHashMap<>();
    private final Map<Symbol, BigDecimal> previousClose = new ConcurrentHashMap<>();

    public SimulatedQuoteFeed(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String name() {
        return "simulated";
    }

    @Override
    public Optional<Quote> latestQuote(Instrument instrument) {
        Symbol symbol = instrument.symbol();
        Currency currency = currencyOf(instrument);
        BigDecimal anchor = anchorPrice(symbol);
        previousClose.putIfAbsent(symbol, anchor);
        BigDecimal price = lastPrice.compute(symbol, (key, current) -> step(key, current == null ? anchor : current));
        return Optional.of(new Quote(
                symbol,
                Money.of(price, currency),
                Money.of(previousClose.get(symbol), currency),
                MarketState.OPEN,
                clock.instant()));
    }

    /**
     * A currency pair is quoted in its second currency; everything else in the instrument's own.
     *
     * <p>Without this, an invented exchange rate would arrive labelled with the wrong currency and
     * quietly distort every converted portfolio total.
     */
    private static Currency currencyOf(Instrument instrument) {
        Matcher pair = CURRENCY_PAIR.matcher(instrument.symbol().value());
        if (pair.matches()) {
            try {
                return Currency.getInstance(pair.group(2));
            } catch (IllegalArgumentException e) {
                return instrument.currency();
            }
        }
        return instrument.currency();
    }

    @Override
    public PriceHistory history(Instrument instrument, HistoryRange range) {
        Instant now = clock.instant();
        Duration interval = range.interval();
        long steps = Math.clamp(range.length().dividedBy(interval), 2, 400);
        Random random = new Random(instrument.symbol().value().hashCode() * 31L + range.ordinal());
        BigDecimal price = anchorPrice(instrument.symbol())
                .multiply(BigDecimal.valueOf(0.75 + random.nextDouble() * 0.2));
        List<Candle> candles = new ArrayList<>();
        for (long i = steps; i >= 0; i--) {
            BigDecimal open = price;
            BigDecimal close = scale(open.multiply(BigDecimal.valueOf(1 + (random.nextDouble() - 0.48) / 50)));
            BigDecimal high = open.max(close).multiply(BigDecimal.valueOf(1 + random.nextDouble() / 200));
            BigDecimal low = open.min(close).multiply(BigDecimal.valueOf(1 - random.nextDouble() / 200));
            candles.add(new Candle(now.minus(interval.multipliedBy(i)), scale(open), scale(high), scale(low), close,
                    1_000 + random.nextInt(500_000)));
            price = close;
        }
        return new PriceHistory(instrument.symbol(), range, candles);
    }

    /**
     * A stable, symbol-specific base price.
     *
     * <p>Shares land somewhere between 10 and 810; currency pairs get a rate between 0.5 and 1.5, so
     * a converted total stays in the region a real one would be.
     */
    private static BigDecimal anchorPrice(Symbol symbol) {
        int hash = Math.abs(symbol.value().hashCode());
        if (CURRENCY_PAIR.matcher(symbol.value()).matches()) {
            return scale(BigDecimal.valueOf(0.5 + (hash % 100) / 100.0));
        }
        return scale(BigDecimal.valueOf(10 + hash % 800).add(BigDecimal.valueOf(hash % 100 / 100.0)));
    }

    private BigDecimal step(Symbol symbol, BigDecimal current) {
        Random random = new Random(symbol.value().hashCode() ^ clock.millis());
        BigDecimal drift = current
                .multiply(MAX_STEP_PERCENT)
                .multiply(BigDecimal.valueOf((random.nextDouble() - 0.5) / 100));
        BigDecimal next = current.add(drift);
        // Keep the walk from drifting into nonsense over a long-running demo.
        BigDecimal anchor = anchorPrice(symbol);
        if (next.compareTo(anchor.multiply(BigDecimal.valueOf(0.5))) < 0
                || next.compareTo(anchor.multiply(BigDecimal.valueOf(1.5))) > 0) {
            next = anchor;
        }
        return scale(next);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
