package com.bewi.stockmanager.marketdata.adapter.in.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.bewi.stockmanager.marketdata.adapter.in.web.dto.InstrumentView;
import com.bewi.stockmanager.marketdata.application.port.in.InstrumentCatalog;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteQuery;
import com.bewi.stockmanager.marketdata.domain.HistoryRange;
import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.PriceHistory;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * The market data system's own UI.
 *
 * <p>Every self-contained system brings its own web front end; the portfolio system links here and
 * embeds this system's quote widget rather than rendering market data itself.
 */
@Controller
class InstrumentWebController {

    private static final int SEARCH_RESULTS = 30;

    private final InstrumentCatalog catalog;
    private final QuoteQuery quotes;

    InstrumentWebController(InstrumentCatalog catalog, QuoteQuery quotes) {
        this.catalog = catalog;
        this.quotes = quotes;
    }

    @GetMapping("/")
    String index(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("instruments", search(query));
        return "instruments/index";
    }

    /** Fragment endpoint for the search box, so typing does not reload the page. */
    @GetMapping("/instruments/search")
    String search(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("instruments", search(query));
        return "instruments/index :: results";
    }

    @GetMapping("/instruments/{symbol}")
    String detail(@PathVariable String symbol,
            @RequestParam(value = "range", defaultValue = "1m") String range, Model model) {
        Symbol ticker = Symbol.of(symbol);
        Instrument instrument = catalog.bySymbol(ticker).orElseGet(() -> Instrument.builder(ticker).build());
        PriceHistory history = quotes.historyFor(ticker, HistoryRange.fromCode(range));

        model.addAttribute("instrument", InstrumentView.from(instrument));
        model.addAttribute("history", InstrumentView.HistoryView.from(history));
        return "instruments/detail";
    }

    private List<InstrumentView> search(String query) {
        return catalog.search(query, SEARCH_RESULTS).stream().map(InstrumentView::from).toList();
    }
}
