package com.trianel.masterdata.market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MArketTest {

    private MarketRepository marketRepository;

    @BeforeEach
    void setUp() {
        marketRepository = new InMemoryMarketRepository();
    }

    @Test
    void createMarket() {
        Market deutschland = Market.builder().name("Deutschland").build();

        assertTrue(deutschland.getId() != null);
        assertEquals(marketRepository.save(deutschland).getId(), deutschland.getId());

        Market NL = Market.builder().id(UUID.randomUUID()).name("NL").build();
        marketRepository.save(NL);

        assertEquals(2, marketRepository.findAll().size());

    }
}
