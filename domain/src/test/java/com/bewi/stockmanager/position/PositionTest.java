package com.bewi.stockmanager.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Position SUT;

    @BeforeEach
    void setUp() {

        SUT = Position.builder()
                .name("Amazon")
                .quantity(100)
                .wkn("A12345")
                .strikeprice(1000)
                .build();
    }

    @Test
    void getName() {
        assertEquals("Amazon", SUT.getName());
    }

    @Test
    void addTranche() {
        assertEquals(100, SUT.getQuantity());
        SUT.addTranche(new Tranche(75, 99, OffsetDateTime.now()));
        assertEquals(199, SUT.getQuantity());
    }




}