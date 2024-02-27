package com.bewi.stockmanager.position.dto;

import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.SubPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PositionDTOMapper {

    PositionDTOMapper INSTANCE = Mappers.getMapper(PositionDTOMapper.class);

    PositionDTO toDTO(Position position);

    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "strikeDate", ignore = true)
    Position toDomain(PositionDTO positionDTO);

    SubPositionDTO toDTO(SubPosition subPosition);

    SubPosition toDomain(SubPositionDTO subPositionDTO);
}
