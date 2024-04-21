package com.bewi.stockmanager.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionServiceTest {

    private PositionRepository positionRepository;
    private PositionService SUT;

    @BeforeEach
    void setUp() {
        positionRepository = new InMemoryPositionRepository();
        SUT = new PositionService(positionRepository);
    }

    @Test
    void opensPosition() {
        Position amazon1 = Position.builder()
                .name("Amazon")
                .quantity(100)
                .strikeprice(1000)
                .wkn("A12345")
                .build();

        positionRepository.save(amazon1);

        Position amazon2 = Position.builder()
                .name("Amazon")
                .quantity(2)
                .strikeprice(33)
                .wkn("A12345")
                .build();

        amazon2.addTranche(new Tranche(77, 8, OffsetDateTime.now()));

        Position position1 = SUT.openPosition(amazon2);
        assertEquals(110, position1.getQuantity());
        assertEquals(1, positionRepository.findAll().size());
        Position position = positionRepository.findByWKN("A12345").get();
        assertEquals(3, position.getTranches().size());
    }
}
