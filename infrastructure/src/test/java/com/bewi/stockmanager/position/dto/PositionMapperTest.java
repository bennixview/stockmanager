package com.bewi.stockmanager.position.dto;

import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.Tranche;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionMapperTest {

    Position testPosition;
    PositionMapper SUT;

    @BeforeEach
    void setUp() {
        SUT = PositionMapper.INSTANCE;
        testPosition = Position.builder()
                .name("Amazon")
                .quantity(100)
                .wkn("A12345")
                .strikeprice(1000)
                .build();
        testPosition.addTranche(new Tranche(999, 100, OffsetDateTime.now()));
    }

    @Test
    void testDomainToDTO() {
        assertEquals(2, testPosition.getTranches().size());
        assertEquals(200, testPosition.getQuantity());

        PositionDTO positionDTO = SUT.toDTO(testPosition);
        assertEquals(2, positionDTO.tranches.size());
        assertEquals(200, positionDTO.quantity);

    }


    @Test
    void testDTOtoDomain() {
        PositionDTO dto = new PositionDTO();
        dto.id = UUID.randomUUID().toString();
        dto.wkn = "A123456";
        dto.isin = "DE1234567890";
        dto.name = "Amazon";
        dto.quantity = 10;
        dto.strikeprice = 20;
        TrancheDTO t1 = new TrancheDTO();
        t1.quantity = 4;
        t1.strikeprice = 20;
        t1.strikeDate = OffsetDateTime.now();
        TrancheDTO t2 = new TrancheDTO();
        t2.quantity = 6;
        t2.strikeprice = 20;
        t2.strikeDate = OffsetDateTime.now().minusDays(1L);

        dto.tranches = Set.of(t1, t2);

        Position domainPosition = SUT.toDomain(dto);

        assertEquals(2, domainPosition.getTranches().size());
        assertEquals(10, domainPosition.getQuantity());
    }
}