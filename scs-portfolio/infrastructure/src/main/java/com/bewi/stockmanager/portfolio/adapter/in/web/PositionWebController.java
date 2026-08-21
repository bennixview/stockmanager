package com.bewi.stockmanager.portfolio.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bewi.stockmanager.portfolio.adapter.in.web.dto.PositionDetailView;
import com.bewi.stockmanager.portfolio.application.port.in.TradeInstruments;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;
import com.bewi.stockmanager.portfolio.domain.PositionId;

/** The position detail page. */
@Controller
class PositionWebController {

    private final ViewPortfolio viewPortfolio;
    private final TradeInstruments trades;

    PositionWebController(ViewPortfolio viewPortfolio, TradeInstruments trades) {
        this.viewPortfolio = viewPortfolio;
        this.trades = trades;
    }

    @GetMapping("/portfolios/{portfolioId}/positions/{positionId}")
    String details(@PathVariable String portfolioId, @PathVariable String positionId, Model model) {
        model.addAttribute("details", PositionDetailView.from(
                viewPortfolio.position(PortfolioId.of(portfolioId), PositionId.of(positionId))));
        return "portfolio/position";
    }

    @PostMapping("/portfolios/{portfolioId}/positions/{positionId}/delete")
    String delete(@PathVariable String portfolioId, @PathVariable String positionId, RedirectAttributes redirect) {
        trades.removePosition(PortfolioId.of(portfolioId), PositionId.of(positionId));
        redirect.addFlashAttribute("message", "Position removed");
        return "redirect:/portfolios/" + portfolioId;
    }
}
