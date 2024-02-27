package com.bewi.stockmanager.position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Position SUT;

    @BeforeEach
    void setUp() {

        SUT = Position.builder()
                .name("Amazon")
                .quantity(100)
                .strikeprice(1000)
                .build();
    }

    @Test
    void getName() {
        assertEquals("Amazon", SUT.getName());
    }




}