package com.trianel.masterdata.market.dto;

import com.trianel.masterdata.market.Market;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface MarketMapper {
    MarketMapper INSTANCE = Mappers.getMapper(MarketMapper.class);

    @Mapping(target = "id", source = "id", qualifiedByName = "UUIDToString")
    MarketDTO toDTO(Market market);

    @Mapping(target = "id", source = "id", qualifiedByName = "StringToUUID")
    Market toDomain(MarketDTO marketDTO);

    @Named("UUIDToString")
    default String UUIDToString(UUID id) {
        return id.toString();
    }

    @Named("StringToUUID")
    default UUID StringToUUID(String id) {
        return id != null ? UUID.fromString(id) : null;
    }
}
