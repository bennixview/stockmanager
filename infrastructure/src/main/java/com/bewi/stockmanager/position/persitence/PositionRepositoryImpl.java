package com.bewi.stockmanager.position.persitence;

import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import com.bewi.stockmanager.position.dto.PositionDTO;
import com.bewi.stockmanager.position.dto.PositionDTOMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepository {

    private final Set<Position> positions = new HashSet<>();
    private final PositionDTOMapper positionDTOMapper;
    private final String filePath = "positions.json";

    private Set<Position> getPositions() {
        if (positions.isEmpty()) {

            List<PositionDTO> positionDTOS = readPositionsFromFile(filePath);


            positions.addAll(positionDTOS.stream().map(positionDTOMapper::toDomain).toList());
            System.out.println("Read positions: " + positions);
        }
        return positions;
    }





    @Override
    public Position save(Position position) {
        getPositions().add(position);
        writePositionsToFile(getPositions().stream().map(positionDTOMapper::toDTO).toList(), filePath);
        System.out.println("Updated positions written back to file.");
        return position;
    }

    @Override
    public Collection<Position> findAll() {
        return getPositions().stream().toList();
    }

    @Override
    public Optional<Position> findByVin(String vin) {
        return Optional.empty();
    }

    @Override
    public void delete(Position Position) {

    }

    private static List<PositionDTO> readPositionsFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(); // Return an empty list in case of error
        }
    }

    private static void writePositionsToFile(List<PositionDTO> positions, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ignore null fields
        try {
            objectMapper.writeValue(new File(filePath), positions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
