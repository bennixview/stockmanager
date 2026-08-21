package com.bewi.stockmanager.portfolio.adapter.in.legacy;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.out.MarketDataPort;
import com.bewi.stockmanager.portfolio.domain.InstrumentRef;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.Symbol;
import com.bewi.stockmanager.portfolio.domain.Wkn;

/**
 * Reads the {@code positions.json} of the previous version into the database.
 *
 * <p>A driving adapter: it only speaks through the same use cases the web does. The old file knew
 * WKNs but no ticker symbols, so each WKN is resolved against the market data catalogue; positions
 * that cannot be resolved are still imported, under their WKN, and simply stay unpriced until the
 * user corrects them.
 */
public class LegacyPositionsImporter {

    private static final Logger log = LoggerFactory.getLogger(LegacyPositionsImporter.class);

    private final TradeInstruments trades;
    private final ManagePortfolios portfolios;
    private final MarketDataPort marketData;
    private final ZoneId zone;

    /** Reads the legacy shape only, so it brings its own lenient mapper rather than the shared one. */
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public LegacyPositionsImporter(TradeInstruments trades, ManagePortfolios portfolios, MarketDataPort marketData,
            ZoneId zone) {
        this.trades = trades;
        this.portfolios = portfolios;
        this.marketData = marketData;
        this.zone = zone;
    }

    /**
     * Imports every position of the file into the given portfolio.
     *
     * @return how many tranches were booked
     */
    public int importFrom(Path file, Portfolio target) {
        List<LegacyPosition> legacyPositions = read(file);
        int booked = 0;
        for (LegacyPosition legacy : legacyPositions) {
            if (legacy.name() == null || legacy.tranches() == null) {
                continue;
            }
            InstrumentRef instrument = resolve(legacy);
            for (LegacyTranche tranche : legacy.tranches()) {
                if (tranche.quantity() <= 0 || tranche.strikeprice() <= 0) {
                    continue; // the old model allowed empty tranches; they carry no information
                }
                trades.buy(new TradeInstruments.BuyOrder(
                        target.id(),
                        instrument.symbol().value(),
                        instrument.name(),
                        BigDecimal.valueOf(tranche.quantity()),
                        BigDecimal.valueOf(tranche.strikeprice()),
                        BigDecimal.ZERO,
                        instrument.currency().getCurrencyCode(),
                        tradeDate(tranche)));
                booked++;
            }
        }
        log.info("Imported {} tranches from {} into portfolio '{}'", booked, file, target.name());
        return booked;
    }

    /** Imports into the default portfolio; used on first start. */
    public int importFrom(Path file) {
        return importFrom(file, portfolios.defaultPortfolio());
    }

    private InstrumentRef resolve(LegacyPosition legacy) {
        Optional<InstrumentRef> byWkn = wknOf(legacy).flatMap(marketData::byWkn);
        if (byWkn.isPresent()) {
            return byWkn.get();
        }
        // No catalogue entry: keep the position under its WKN so nothing is lost.
        String fallbackSymbol = wknOf(legacy).map(Wkn::value).orElseGet(() -> sanitize(legacy.name()));
        log.info("No instrument found for '{}' (WKN {}), importing it as '{}'",
                legacy.name(), legacy.wkn(), fallbackSymbol);
        return InstrumentRef.of(Symbol.of(fallbackSymbol), legacy.name(), java.util.Currency.getInstance("EUR"));
    }

    private static Optional<Wkn> wknOf(LegacyPosition legacy) {
        if (legacy.wkn() == null || legacy.wkn().isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Wkn.of(legacy.wkn()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Turns a name into something usable as a ticker when there is no better identifier. */
    private static String sanitize(String name) {
        String cleaned = name.toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return cleaned.isEmpty() ? "UNKNOWN" : cleaned.substring(0, Math.min(cleaned.length(), 20));
    }

    /**
     * The trade date of a legacy tranche, or {@code null} when the old file had none - the use case
     * owns the clock and fills today in.
     */
    private LocalDate tradeDate(LegacyTranche tranche) {
        if (tranche.strikeDate() == null) {
            return null;
        }
        // The old format wrote epoch seconds with a fractional part.
        return Instant.ofEpochSecond(tranche.strikeDate().longValue()).atZone(zone).toLocalDate();
    }

    private List<LegacyPosition> read(Path file) {
        try {
            return objectMapper.readValue(Files.readAllBytes(file), new TypeReference<List<LegacyPosition>>() {
            });
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not read legacy positions from " + file, e);
        }
    }

    record LegacyPosition(String id, String name, String wkn, String isin, List<LegacyTranche> tranches) {
    }

    record LegacyTranche(int strikeprice, int quantity, BigDecimal strikeDate) {
    }
}
