package com.bewi.stockmanager.portfolio.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.bewi.stockmanager.portfolio.domain.InsufficientSharesException;
import com.bewi.stockmanager.portfolio.domain.PortfolioNotFoundException;
import com.bewi.stockmanager.portfolio.domain.PositionNotFoundException;

/**
 * Turns domain failures into HTTP answers instead of stack traces.
 *
 * <p>Bound to the API controller by type, not by package: the HTML controllers live next door and
 * want a page rather than a problem document.
 */
@RestControllerAdvice(assignableTypes = PortfolioApiController.class)
class PortfolioExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioExceptionHandler.class);

    @ExceptionHandler({PortfolioNotFoundException.class, PositionNotFoundException.class})
    ProblemDetail notFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InsufficientSharesException.class)
    ProblemDetail insufficientShares(InsufficientSharesException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Not enough shares");
        problem.setProperty("symbol", e.symbol().value());
        problem.setProperty("held", e.held().value());
        problem.setProperty("requested", e.requested().value());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        log.debug("Rejected request: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
