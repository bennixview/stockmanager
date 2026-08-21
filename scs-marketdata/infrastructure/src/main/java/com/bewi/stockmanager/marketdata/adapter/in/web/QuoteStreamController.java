package com.bewi.stockmanager.marketdata.adapter.in.web;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.bewi.stockmanager.marketdata.adapter.in.web.dto.QuoteResponse;
import com.bewi.stockmanager.marketdata.application.port.in.QuoteSubscriptions;
import com.bewi.stockmanager.marketdata.domain.Symbol;

/**
 * Pushes quotes to browsers over server-sent events.
 *
 * <p>SSE rather than WebSockets: the data only flows one way, it survives proxies, and the browser
 * reconnects on its own - which keeps the client side of live data down to a few lines.
 */
@RestController
@RequestMapping("/api/quotes")
@Tag(name = "Quotes", description = "Live prices and price history")
class QuoteStreamController {

    private static final Logger log = LoggerFactory.getLogger(QuoteStreamController.class);
    private static final long STREAM_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final QuoteSubscriptions subscriptions;

    QuoteStreamController(QuoteSubscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Operation(summary = "Stream of live quotes for the given symbols (server-sent events)")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream(@RequestParam("symbols") List<String> symbols) {
        Set<Symbol> watched = symbols.stream().map(Symbol::of).collect(Collectors.toSet());
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);

        QuoteSubscriptions.Subscription subscription = subscriptions.subscribe(watched, quote -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("quote")
                        .id(quote.symbol().value() + "@" + quote.asOf().toEpochMilli())
                        .data(QuoteResponse.from(quote)));
            } catch (IOException | IllegalStateException e) {
                // The client is gone; throwing unsubscribes us.
                throw new SubscriberGoneException(e);
            }
        });

        emitter.onCompletion(subscription::close);
        emitter.onTimeout(() -> {
            subscription.close();
            emitter.complete();
        });
        emitter.onError(error -> subscription.close());
        log.debug("Opened quote stream for {}", watched);
        return emitter;
    }

    /** Signals a dead client so the application layer drops the subscription. */
    private static final class SubscriberGoneException extends RuntimeException {

        private SubscriberGoneException(Throwable cause) {
            super(cause);
        }
    }
}
