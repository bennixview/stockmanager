package com.bewi.stockmanager.marketdata.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bewi.stockmanager.marketdata.domain.UnknownInstrumentException;

/**
 * Turns rejected input into a 400 instead of a 500.
 *
 * <p>The domain's value objects validate on construction, so a malformed symbol or ISIN arrives
 * here as an {@link IllegalArgumentException} - which is a client error, not a server fault.
 */
@RestControllerAdvice(assignableTypes = {QuoteApiController.class, QuoteStreamController.class, InstrumentApiController.class})
class MarketDataExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MarketDataExceptionHandler.class);

    @ExceptionHandler(UnknownInstrumentException.class)
    ProblemDetail unknownInstrument(UnknownInstrumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        log.debug("Rejected request: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
