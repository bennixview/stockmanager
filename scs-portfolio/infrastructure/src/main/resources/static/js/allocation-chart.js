/*
 * Donut chart of what the portfolio is made of.
 *
 * Drawn by hand on a canvas so the page pulls in no third-party script and works offline.
 */
(() => {
    'use strict';

    const palette = ['#60a5fa', '#4ade80', '#f472b6', '#fbbf24', '#a78bfa', '#22d3ee', '#fb923c', '#94a3b8'];

    function draw() {
        const canvas = document.getElementById('allocation');
        const legend = document.getElementById('allocation-legend');
        if (!canvas) {
            return;
        }
        let slices = [];
        try {
            slices = JSON.parse(canvas.dataset.allocation || '[]');
        } catch (error) {
            return;
        }
        slices = slices.filter(slice => Number(slice.value) > 0).sort((a, b) => b.value - a.value);
        const total = slices.reduce((sum, slice) => sum + Number(slice.value), 0);
        if (total <= 0) {
            canvas.replaceWith(Object.assign(document.createElement('p'),
                { className: 'text-secondary mb-0', textContent: 'Noch nichts zu verteilen.' }));
            return;
        }

        const ratio = window.devicePixelRatio || 1;
        const size = Math.min(canvas.clientWidth, Number(canvas.getAttribute('height')) || 260);
        canvas.width = size * ratio;
        canvas.height = size * ratio;
        canvas.style.height = `${size}px`;
        const context = canvas.getContext('2d');
        context.scale(ratio, ratio);

        const center = size / 2;
        const outer = center - 4;
        const inner = outer * 0.62;
        let start = -Math.PI / 2;

        slices.forEach((slice, index) => {
            const angle = (Number(slice.value) / total) * Math.PI * 2;
            context.beginPath();
            context.arc(center, center, outer, start, start + angle);
            context.arc(center, center, inner, start + angle, start, true);
            context.closePath();
            context.fillStyle = palette[index % palette.length];
            context.fill();
            start += angle;
        });

        if (legend) {
            legend.innerHTML = slices.map((slice, index) => `
                <li class="d-flex justify-content-between align-items-center py-1">
                    <span>
                        <span style="display:inline-block;width:.7rem;height:.7rem;border-radius:2px;background:${palette[index % palette.length]}"></span>
                        <span class="ms-2">${slice.label}</span>
                    </span>
                    <span class="text-secondary">${((Number(slice.value) / total) * 100).toFixed(1)} %</span>
                </li>`).join('');
        }
    }

    document.addEventListener('DOMContentLoaded', draw);
    window.addEventListener('resize', draw);
})();
