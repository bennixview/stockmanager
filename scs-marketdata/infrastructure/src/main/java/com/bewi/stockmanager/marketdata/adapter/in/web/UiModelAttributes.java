package com.bewi.stockmanager.marketdata.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Model attributes every page of this system needs.
 *
 * <p>The link back to the portfolio system is a configured URL: self-contained systems integrate
 * through links, and neither has to know how the other is built.
 */
@ControllerAdvice
class UiModelAttributes {

    private final String portfolioUrl;
    private final String quoteProvider;

    UiModelAttributes(@Value("${marketdata.portfolio-url:http://localhost:8080}") String portfolioUrl,
            @Value("${marketdata.provider:simulated}") String quoteProvider) {
        this.portfolioUrl = portfolioUrl;
        this.quoteProvider = quoteProvider;
    }

    @ModelAttribute
    void addGlobals(Model model) {
        model.addAttribute("portfolioUrl", portfolioUrl);
        model.addAttribute("quoteProvider", quoteProvider);
    }
}
