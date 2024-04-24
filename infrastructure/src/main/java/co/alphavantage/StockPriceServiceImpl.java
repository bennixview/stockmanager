package co.alphavantage;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Component
public class StockPriceServiceImpl {
    private final RestTemplate restTemplate;

    private final Map<String, String> priceOfSymbol = new HashMap<>();
    private final List<String> symbols = List.of("AMNZ");

    @Autowired
    public StockPriceServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    @Value("${alpha.vantage.url}")
    private String baseUrl;

    //@Scheduled(fixedRate = 60000) // 300,000 milliseconds = 5 minutes
    @Scheduled(cron = "0 * * * * *")
    public void fetchStockPriceScheduled() {
        fetchStockPrice();
    }

    public void fetchStockPrice() {
        symbols.forEach(s -> {
            priceOfSymbol.put(s, fetchStockPriceBySmbol(s));
        });
    }

    private String fetchStockPriceBySmbol(@Nonnull String symbol) {
        String url = baseUrl + "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
        System.out.println("Call: " + url);
        //TODO Map<String, String> result = restTemplate.getForObject(url, Map.class);
        //  System.out.println("Stock price: " + result.get("05. price"));
        //  System.out.println("Symbol Namee: " + result.get("01. symbol"));
        return "42." + 42.0 + new java.util.Random().nextDouble() * (100.0 - 42.0); //TODO result.get("05. price");
    }
}
