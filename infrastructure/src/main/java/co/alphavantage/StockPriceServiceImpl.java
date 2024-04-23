package co.alphavantage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class StockPriceServiceImpl {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    @Value("${alpha.vantage.url}")
    private String baseUrl;

    //@Scheduled(fixedRate = 60000) // 300,000 milliseconds = 5 minutes
    @Scheduled(cron = "0 * * * * *")
    public void fetchStockPrice() {
        String symbol = "AMZN"; // Replace with dynamic symbol if needed
        String url = baseUrl + "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
        System.out.println("Call: " + url);
        Map<String, String> result = restTemplate.getForObject(url, Map.class);
        System.out.println("Stock price: " + result.get("05. price"));
        System.out.println("Symbol Namee: " + result.get("01. symbol"));
    }
}
