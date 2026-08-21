package com.bewi.stockmanager.portfolio.adapter.in.web;

import java.util.Currency;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bewi.stockmanager.portfolio.adapter.in.web.dto.PortfolioView;
import com.bewi.stockmanager.portfolio.adapter.in.web.form.PortfolioForm;
import com.bewi.stockmanager.portfolio.application.port.in.ManagePortfolios;
import com.bewi.stockmanager.portfolio.application.port.in.ViewPortfolio;
import com.bewi.stockmanager.portfolio.domain.Portfolio;
import com.bewi.stockmanager.portfolio.domain.PortfolioId;

/**
 * The pages that show a portfolio.
 *
 * <p>Server-rendered HTML: the page is complete without JavaScript, and the live prices layered on
 * top by the market data system's widget are an enhancement, not a requirement.
 */
@Controller
class PortfolioWebController {

    private final ViewPortfolio viewPortfolio;
    private final ManagePortfolios managePortfolios;

    PortfolioWebController(ViewPortfolio viewPortfolio, ManagePortfolios managePortfolios) {
        this.viewPortfolio = viewPortfolio;
        this.managePortfolios = managePortfolios;
    }

    @GetMapping("/")
    String dashboard(Model model) {
        model.addAttribute("portfolio", PortfolioView.from(viewPortfolio.defaultOverview()));
        model.addAttribute("portfolioForm", new PortfolioForm());
        return "portfolio/dashboard";
    }

    @GetMapping("/portfolios/{portfolioId}")
    String portfolio(@PathVariable String portfolioId, Model model) {
        model.addAttribute("portfolio",
                PortfolioView.from(viewPortfolio.overview(PortfolioId.of(portfolioId))));
        model.addAttribute("portfolioForm", new PortfolioForm());
        return "portfolio/dashboard";
    }

    @PostMapping("/portfolios")
    String create(@Valid @ModelAttribute("portfolioForm") PortfolioForm form, BindingResult binding, Model model,
            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("portfolio", PortfolioView.from(viewPortfolio.defaultOverview()));
            return "portfolio/dashboard";
        }
        Portfolio created = managePortfolios.createPortfolio(form.getName(), Currency.getInstance(form.getCurrency()));
        redirect.addFlashAttribute("message", "Portfolio '%s' created".formatted(created.name()));
        return "redirect:/portfolios/" + created.id();
    }

    @PostMapping("/portfolios/{portfolioId}/rename")
    String rename(@PathVariable String portfolioId, @Valid @ModelAttribute("portfolioForm") PortfolioForm form,
            BindingResult binding, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            managePortfolios.renamePortfolio(PortfolioId.of(portfolioId), form.getName());
            redirect.addFlashAttribute("message", "Portfolio renamed");
        }
        return "redirect:/portfolios/" + portfolioId;
    }

    @PostMapping("/portfolios/{portfolioId}/delete")
    String delete(@PathVariable String portfolioId, RedirectAttributes redirect) {
        managePortfolios.deletePortfolio(PortfolioId.of(portfolioId));
        redirect.addFlashAttribute("message", "Portfolio deleted");
        return "redirect:/";
    }
}
