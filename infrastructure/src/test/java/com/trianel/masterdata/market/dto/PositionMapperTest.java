package com.trianel.masterdata.market.dto;

import com.trianel.masterdata.market.Market;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PositionMapperTest {

    Market testMarket;
    MarketMapper SUT;

    @BeforeEach
    void setUp() {
        SUT = MarketMapper.INSTANCE.INSTANCE;
        testMarket = Market.builder().name("Deutschland").build();
    }

    @Test
    void testDomainToDTO() {
        assertEquals("Deutschland", testMarket.getName());
        assertNotNull(testMarket.getId());

        MarketDTO marketDTO = SUT.toDTO(testMarket);
        assertEquals("Deutschland", marketDTO.name);
        assertEquals(testMarket.getId(), marketDTO.id);

    }


    @Test
    void testDTOtoDomain() {
        MarketDTO dto = new MarketDTO();
        dto.id = UUID.randomUUID();
        dto.name = "Deutschland";

        Market market = SUT.toDomain(dto);

        assertEquals(dto.name, market.getName());
        assertEquals(dto.id, market.getId());
    }
}