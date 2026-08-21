/*
 * <market-quote symbol="AAPL"> - the market data system's UI contribution.
 *
 * Other self-contained systems embed this script from this system's origin and use the element in
 * their own pages. Nothing about prices is re-implemented on their side: markup, formatting and the
 * live connection all stay here, and this file can change shape without a coordinated release.
 *
 *   <script src="http://localhost:8081/js/market-quote.js"></script>
 *   <market-quote symbol="AAPL" show="full"></market-quote>
 *
 * show="compact" renders price and percentage, show="full" adds the absolute change.
 */
(() => {
    'use strict';

    const origin = new URL(document.currentScript.src).origin;

    /**
     * One event stream for the whole page.
     *
     * A dashboard has dozens of quote elements; opening one connection each would exhaust the
     * browser's per-origin connection limit, so subscriptions are collected and multiplexed.
     */
    const stream = {
        symbols: new Set(),
        listeners: new Map(),
        source: null,
        reopenHandle: null,

        subscribe(symbol, element) {
            if (!this.listeners.has(symbol)) {
                this.listeners.set(symbol, new Set());
            }
            this.listeners.get(symbol).add(element);
            if (!this.symbols.has(symbol)) {
                this.symbols.add(symbol);
                this.scheduleReopen();
            }
        },

        unsubscribe(symbol, element) {
            const elements = this.listeners.get(symbol);
            if (!elements) {
                return;
            }
            elements.delete(element);
            if (elements.size === 0) {
                this.listeners.delete(symbol);
                this.symbols.delete(symbol);
                this.scheduleReopen();
            }
        },

        // Elements connect one by one while the page parses; wait a tick and open a single stream.
        scheduleReopen() {
            clearTimeout(this.reopenHandle);
            this.reopenHandle = setTimeout(() => this.reopen(), 50);
        },

        reopen() {
            if (this.source) {
                this.source.close();
                this.source = null;
            }
            if (this.symbols.size === 0) {
                return;
            }
            const symbols = [...this.symbols].join(',');
            this.prime(symbols);
            this.source = new EventSource(`${origin}/api/quotes/stream?symbols=${encodeURIComponent(symbols)}`);
            this.source.addEventListener('quote', event => this.dispatch(JSON.parse(event.data)));
            // EventSource reconnects by itself; nothing to do on error but let it.
        },

        // The stream only sends changes, so fetch the current state once for an immediate render.
        prime(symbols) {
            fetch(`${origin}/api/quotes?symbols=${encodeURIComponent(symbols)}`)
                .then(response => (response.ok ? response.json() : []))
                .then(quotes => quotes.forEach(quote => this.dispatch(quote)))
                .catch(() => this.failAll());
        },

        dispatch(quote) {
            (this.listeners.get(quote.symbol) || []).forEach(element => element.update(quote));
        },

        failAll() {
            this.listeners.forEach(elements => elements.forEach(element => element.fail()));
        }
    };

    class MarketQuote extends HTMLElement {

        static get observedAttributes() {
            return ['symbol'];
        }

        connectedCallback() {
            this.symbol = (this.getAttribute('symbol') || '').toUpperCase();
            this.mode = this.getAttribute('show') || 'compact';
            if (!this.symbol) {
                this.textContent = '';
                return;
            }
            this.innerHTML = '<span class="market-quote-loading text-secondary">…</span>';
            stream.subscribe(this.symbol, this);
        }

        disconnectedCallback() {
            stream.unsubscribe(this.symbol, this);
        }

        attributeChangedCallback(name, previous, current) {
            if (name === 'symbol' && previous && previous !== current && this.isConnected) {
                stream.unsubscribe(previous.toUpperCase(), this);
                this.connectedCallback();
            }
        }

        update(quote) {
            const rising = Number(quote.changePercent) >= 0;
            const tone = rising ? 'market-quote-up' : 'market-quote-down';
            const sign = rising ? '+' : '';
            const price = formatMoney(quote.price, quote.currency);
            const percent = `${sign}${Number(quote.changePercent).toFixed(2)} %`;
            const absolute = `${sign}${formatMoney(quote.change, quote.currency)}`;

            this.innerHTML = this.mode === 'full'
                ? `<span class="market-quote-price">${price}</span>
                   <span class="market-quote-change ${tone}">${absolute} (${percent})</span>`
                : `<span class="market-quote-price">${price}</span>
                   <span class="market-quote-change ${tone}">${percent}</span>`;
            this.title = `${quote.symbol} · ${quote.marketState.toLowerCase()} · ${new Date(quote.asOf).toLocaleTimeString()}`;
            this.dataset.direction = rising ? 'up' : 'down';
            this.flash();
        }

        fail() {
            this.innerHTML = '<span class="market-quote-error text-secondary" title="No price available">n/a</span>';
        }

        flash() {
            this.classList.remove('market-quote-flash');
            void this.offsetWidth; // restart the CSS animation
            this.classList.add('market-quote-flash');
        }
    }

    function formatMoney(amount, currency) {
        return new Intl.NumberFormat(navigator.language || 'de-DE', {
            style: 'currency',
            currency: currency || 'EUR'
        }).format(Number(amount));
    }

    // The element carries its own styling so embedding systems need no stylesheet from us.
    const style = document.createElement('style');
    style.textContent = `
        market-quote { display: inline-flex; gap: .5rem; align-items: baseline; white-space: nowrap; font-variant-numeric: tabular-nums; }
        market-quote .market-quote-price { font-weight: 600; }
        market-quote .market-quote-change { font-size: .85em; }
        market-quote .market-quote-up { color: #4ade80; }
        market-quote .market-quote-down { color: #f87171; }
        market-quote.market-quote-flash { animation: market-quote-flash .6s ease-out; }
        @keyframes market-quote-flash { from { background-color: rgba(148, 163, 184, .35); } to { background-color: transparent; } }
    `;
    document.head.appendChild(style);

    if (!customElements.get('market-quote')) {
        customElements.define('market-quote', MarketQuote);
    }
})();
