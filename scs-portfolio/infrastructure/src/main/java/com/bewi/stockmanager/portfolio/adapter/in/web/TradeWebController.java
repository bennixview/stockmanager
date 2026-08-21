package com.bewi.stockmanager.portfolio.adapter.in.web;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bewi.stockmanager.portfolio.adapter.in.web.dto.PositionDetailView;
import com.bewi.stockmanager.portfolio.adapter.in.web.form.BuyForm;
import com.bewi.stockmanager.portfolio.adapter.in.web.form.SellForm;
import com.bewi.stockmanager.portfolio.application.port.in.SearchInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.InsufficientSharesException;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PositionId;
import com.bewi.stockmanager.portfolio.domain.Position;
import com.bewi.stockmanager.portfolio.domain.Symbol;

/** Booking trades from the UI. */
@Controller
class TradeWebController {

    private final TradeInstruments trades;
    private final SearchInstruments instruments;
    private final ViewPortfolio viewPortfolio;

    TradeWebController(TradeInstruments trades, SearchInstruments instruments, ViewPortfolio viewPortfolio) {
        this.trades = trades;
        this.instruments = instruments;
        this.viewPortfolio = viewPortfolio;
    }

    @GetMapping("/portfolios/{portfolioId}/buy")
    String buyForm(@PathVariable String portfolioId,
            @RequestParam(value = "symbol", required = false) String symbol, Model model) {
        BuyForm form = new BuyForm();
        if (symbol != null && !symbol.isBlank()) {
            form.setSymbol(symbol);
            instruments.resolve(Symbol.of(symbol)).ifPresent(instrument -> {
                form.setName(instrument.name());
                form.setCurrency(instrument.currency().getCurrencyCode());
            });
        }
        model.addAttribute("buyForm", form);
        model.addAttribute("portfolioId", portfolioId);
        model.addAttribute("results", java.util.List.of());
        return "portfolio/buy";
    }

    @PostMapping("/portfolios/{portfolioId}/buy")
    String buy(@PathVariable String portfolioId, @Valid @ModelAttribute("buyForm") BuyForm form,
            BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("portfolioId", portfolioId);
            model.addAttribute("results", java.util.List.of());
            return "portfolio/buy";
        }
        Position position = trades.buy(new TradeInstruments.BuyOrder(
                PortfolioId.of(portfolioId),
                form.getSymbol(),
                form.getName(),
                form.getQuantity(),
                form.getPricePerShare(),
                form.getFee(),
                form.getCurrency(),
                form.getTradeDate()));
        redirect.addFlashAttribute("message",
                "Bought %s %s".formatted(form.getQuantity().stripTrailingZeros().toPlainString(), form.getSymbol()));
        return "redirect:/portfolios/%s/positions/%s".formatted(portfolioId, position.id());
    }

    @GetMapping("/portfolios/{portfolioId}/positions/{positionId}/sell")
    String sellForm(@PathVariable String portfolioId, @PathVariable String positionId, Model model) {
        PositionDetailView details = PositionDetailView.from(
                viewPortfolio.position(PortfolioId.of(portfolioId), PositionId.of(positionId)));
        SellForm form = new SellForm();
        form.setSymbol(details.position().symbol());
        form.setQuantity(new java.math.BigDecimal(details.position().quantity()));
        form.setPricePerShare(details.position().currentPrice());
        model.addAttribute("sellForm", form);
        model.addAttribute("details", details);
        model.addAttribute("portfolioId", portfolioId);
        return "portfolio/sell";
    }

    @PostMapping("/portfolios/{portfolioId}/positions/{positionId}/sell")
    String sell(@PathVariable String portfolioId, @PathVariable String positionId,
            @Valid @ModelAttribute("sellForm") SellForm form, BindingResult binding, Model model,
            RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                trades.sell(new TradeInstruments.SellOrder(
                        PortfolioId.of(portfolioId),
                        form.getSymbol(),
                        form.getQuantity(),
                        form.getPricePerShare(),
                        form.getFee(),
                        form.getTradeDate()));
                redirect.addFlashAttribute("message", "Sold %s %s"
                        .formatted(form.getQuantity().stripTrailingZeros().toPlainString(), form.getSymbol()));
                return "redirect:/portfolios/" + portfolioId;
            } catch (InsufficientSharesException e) {
                binding.rejectValue("quantity", "insufficient", e.getMessage());
            }
        }
        model.addAttribute("details", PositionDetailView.from(
                viewPortfolio.position(PortfolioId.of(portfolioId), PositionId.of(positionId))));
        model.addAttribute("portfolioId", portfolioId);
        return "portfolio/sell";
    }

    /** htmx fragment: instrument search results while typing in the buy form. */
    @GetMapping("/instruments/search")
    String search(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("results", instruments.search(query));
        return "portfolio/buy :: results";
    }
}
