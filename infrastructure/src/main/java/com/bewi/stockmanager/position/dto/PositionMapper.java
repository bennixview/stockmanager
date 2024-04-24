package com.bewi.stockmanager.position.dto;

import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.Tranche;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PositionMapper {
    PositionMapper INSTANCE = Mappers.getMapper(PositionMapper.class);

    @Mapping(target = "id", source = "id", qualifiedByName = "UUIDToString")
    @Mapping(target = "price", ignore = true)
    PositionDTO toDTO(Position position);

    @Mapping(target = "id", source = "id", qualifiedByName = "StringToUUID")
    @Mapping(target = "strikeDate", ignore = true)
    @Mapping(target = "strikeprice", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    Position toDomain(PositionDTO positionDTO);

    @Named("UUIDToString")
    default String UUIDToString(UUID id) {
        return id.toString();
    }

    @Named("StringToUUID")
    default UUID StringToUUID(String id) {
        return UUID.fromString(id);
    }

    Tranche toDomain(TrancheDTO trancheDTO);

    TrancheDTO toDTO(Tranche tranche);
}
