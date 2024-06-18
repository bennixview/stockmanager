package com.trianel.masterdata.deliveryscheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeliverySchemeTest {

    private DeliverySchemeRepository DeliverySchemeRepository;

    @BeforeEach
    void setUp() {
        DeliverySchemeRepository = new InMemoryDeliverySchemeRepository();
    }

    @Test
    void createDeliveryScheme() {
        DeliveryScheme deutschland = DeliveryScheme.builder().name("Deutschland").build();

        assertTrue(deutschland.getId() != null);
        assertEquals(DeliverySchemeRepository.save(deutschland).getId(), deutschland.getId());

        DeliveryScheme NL = DeliveryScheme.builder().id(UUID.randomUUID()).name("NL").build();
        DeliverySchemeRepository.save(NL);

        assertEquals(2, DeliverySchemeRepository.findAll().size());

    }
}
