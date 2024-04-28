package com.bewi.stockmanager.position.persitence;

import com.bewi.paging.Page;
import com.bewi.paging.Paged;
import com.bewi.paging.Paging;
import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import com.bewi.stockmanager.position.dto.PositionDTO;
import com.bewi.stockmanager.position.dto.PositionMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is just an example of a Repository that uses for now a json file instead of database.
 * If you want to use JPA or MongoDB Repository it is a good approach to delegate to a seperate
 * Spring interface repository as DAO
 */


@Component
@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepository {

    private final Set<Position> positions = new HashSet<>();
    private final PositionMapper positionMapper;
    private static final String filePath = "positions.json";

    private Set<Position> getPositions() {
        if (positions.isEmpty()) {
            readPositionsFromFile(filePath).forEach(positionDTO -> positions.add(positionMapper.toDomain(positionDTO)));
        }
        return positions;
    }


    @Override
    public Position save(Position position) {
        getPositions().add(position);
        writePositionsToFile(getPositions().stream().map(positionMapper::toDTO).toList());
        System.out.println("Updated positions written back to file.");
        return position;
    }

    @Override
    public Collection<Position> findAll() {
        return getPositions().stream().sorted(Comparator.comparing(Position::getName)).toList();
    }

    @Override
    public Optional<Position> findByWKN(String wkn) {
        return positions.stream().filter(position -> position.getWkn().equals(wkn)).findFirst();
    }

    @Override
    public void delete(Position Position) {

    }

    @Override
    public Optional<Position> findById(UUID id) {
        return positions.stream().filter(position -> position.getId().equals(id)).findFirst();
    }

    public Paged<Position> getPositions(int pageNumber, int size) {
        Collection<Position> allPositions = this.findAll();
        List<Position> paged = allPositions.stream()
                .skip(pageNumber )
                .limit(size)
                .collect(Collectors.toList());
        int totalPages = (allPositions.size() + size - 1) / size;
        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));
    }

    public static List<PositionDTO> readPositionsFromFile(String filePath) {
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

    private static void writePositionsToFile(List<PositionDTO> positions) {
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
