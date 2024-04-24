package co.alphavantage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestPropertySource(properties = {"alpha.vantage.api.key=your_api_key_here", "alpha.vantage.url=your_url_here"})
@Disabled
public class StockPriceServiceImplTest {



    private StockPriceServiceImpl SUT;

    @BeforeEach
    void setUp() {
        SUT = new StockPriceServiceImpl(new RestTemplate());
    }

    @Test
    void testGetStockPrice() {
        SUT.fetchStockPrice();
    }
}
