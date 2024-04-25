class StockPrice extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.updateInterval = 6000; // Update every 5 minutes (300000 milliseconds)
    }

    connectedCallback() {
        this.id = this.getAttribute('id');
        if (!this.id) {
            console.error("StockPrice element requires an 'id' attribute.");
            return;
        }
        this.fetchAndDisplay(); // Initial fetch

        // Set up the interval for scheduled updates
        this.interval = setInterval(() => {
            this.fetchAndDisplay();
        }, this.updateInterval);
    }

    disconnectedCallback() {
        // Clean up the interval when the element is removed from the DOM
        if (this.interval) {
            clearInterval(this.interval);
        }
    }

    fetchAndDisplay() {
        fetch(`/api/positions/${this.id}`)
            .then(response => response.json())
            .then(data => {
                this.render(data);
            })
            .catch(error => {
                console.error('Error fetching stock data:', error);
                this.shadowRoot.innerHTML = `<span>Error loading data</span>`;
            });
    }

    render(data) {
        this.shadowRoot.innerHTML = `
            <style>
                .stock-container {
                   /* background-color: #f8f9fa;
                    border: 1px solid #ced4da;
                    border-radius: 0.25rem;
                    padding: 15px;
                    margin-bottom: 10px;
                    display: flex;
                    flex-direction: column;
                    gap: 10px;*/
                }
                .stock-header {
                    font-size: 18px;
                    font-weight: bold;
                    color: #343a40;
                }
                .stock-info {
                  /*  font-size: 16px;
                    color: #495057;*/
                }
            </style>
            <div class="stock-container">
              <!--div class="stock-header"></div-->
                <div class="stock-info">Strike Price: ${data.price}</div>
           
            </div>
        `;
    }
}

customElements.define('stock-price', StockPrice);
