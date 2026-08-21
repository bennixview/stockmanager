package com.bewi.stockmanager.portfolio.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bewi.stockmanager.portfolio.adapter.in.legacy.LegacyPositionsImporter;
import com.bewi.stockmanager.portfolio.domain.PortfolioRepository;

/**
 * Imports the old {@code positions.json} on first start, and never again.
 *
 * <p>Guarded by the portfolio count rather than by a flag file, so re-running the application after
 * a crash cannot double-book the same trades.
 */
@Component
class LegacyImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyImportRunner.class);

    private final LegacyPositionsImporter importer;
    private final PortfolioRepository portfolios;
    private final PortfolioProperties properties;

    LegacyImportRunner(LegacyPositionsImporter importer, PortfolioRepository portfolios,
            PortfolioProperties properties) {
        this.importer = importer;
        this.portfolios = portfolios;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.legacyImport().enabled()) {
            return;
        }
        if (portfolios.count() > 0) {
            log.debug("Portfolios already exist, skipping the legacy import");
            return;
        }
        Path file = Path.of(properties.legacyImport().file());
        if (!Files.isReadable(file)) {
            log.debug("No legacy positions file at {}, nothing to import", file.toAbsolutePath());
            return;
        }
        try {
            importer.importFrom(file);
        } catch (RuntimeException e) {
            // A broken legacy file must not stop the application from starting.
            log.warn("Could not import legacy positions from {}: {}", file.toAbsolutePath(), e.getMessage());
        }
    }
}
