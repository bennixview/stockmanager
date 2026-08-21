/*
 * Draws the price history onto a canvas.
 *
 * Hand-rolled rather than pulled from a CDN: the page stays self-contained, works offline and adds
 * no third-party origin to the content security policy.
 */
(() => {
    'use strict';

    function draw(canvas) {
        const series = JSON.parse(canvas.dataset.series || '[]').map(Number).filter(Number.isFinite);
        if (series.length < 2) {
            return;
        }
        const ratio = window.devicePixelRatio || 1;
        const width = canvas.clientWidth;
        const height = Number(canvas.getAttribute('height')) || 320;
        canvas.width = width * ratio;
        canvas.height = height * ratio;
        canvas.style.height = `${height}px`;

        const context = canvas.getContext('2d');
        context.scale(ratio, ratio);
        context.clearRect(0, 0, width, height);

        const padding = { top: 12, right: 8, bottom: 20, left: 56 };
        const plotWidth = width - padding.left - padding.right;
        const plotHeight = height - padding.top - padding.bottom;
        const min = Math.min(...series);
        const max = Math.max(...series);
        const span = max - min || 1;
        const x = index => padding.left + (index / (series.length - 1)) * plotWidth;
        const y = value => padding.top + plotHeight - ((value - min) / span) * plotHeight;

        const rising = series[series.length - 1] >= series[0];
        const line = rising ? '#4ade80' : '#f87171';

        context.strokeStyle = 'rgba(148, 163, 184, .2)';
        context.fillStyle = 'rgba(148, 163, 184, .8)';
        context.font = '11px system-ui, sans-serif';
        context.textAlign = 'right';
        for (let step = 0; step <= 4; step++) {
            const value = min + (span * step) / 4;
            const position = y(value);
            context.beginPath();
            context.moveTo(padding.left, position);
            context.lineTo(width - padding.right, position);
            context.stroke();
            context.fillText(value.toFixed(2), padding.left - 8, position + 4);
        }

        const gradient = context.createLinearGradient(0, padding.top, 0, height);
        gradient.addColorStop(0, rising ? 'rgba(74, 222, 128, .35)' : 'rgba(248, 113, 113, .35)');
        gradient.addColorStop(1, 'rgba(0, 0, 0, 0)');

        context.beginPath();
        series.forEach((value, index) => (index === 0 ? context.moveTo(x(index), y(value)) : context.lineTo(x(index), y(value))));
        context.lineTo(x(series.length - 1), padding.top + plotHeight);
        context.lineTo(x(0), padding.top + plotHeight);
        context.closePath();
        context.fillStyle = gradient;
        context.fill();

        context.beginPath();
        series.forEach((value, index) => (index === 0 ? context.moveTo(x(index), y(value)) : context.lineTo(x(index), y(value))));
        context.strokeStyle = line;
        context.lineWidth = 2;
        context.stroke();
    }

    function drawAll() {
        document.querySelectorAll('canvas.price-chart').forEach(draw);
    }

    document.addEventListener('DOMContentLoaded', drawAll);
    window.addEventListener('resize', drawAll);
})();
