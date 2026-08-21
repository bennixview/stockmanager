package com.bewi.stockmanager.portfolio.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.PositionNotFoundException;

/** Shows a page rather than a stack trace when a portfolio or position is gone. */
@ControllerAdvice(assignableTypes = {PortfolioWebController.class, PositionWebController.class,
        TradeWebController.class})
class WebErrorController {

    @ExceptionHandler({PortfolioNotFoundException.class, PositionNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound(RuntimeException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/not-found";
    }
}
