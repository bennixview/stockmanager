package com.bewi.stockmanager.controller;


import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import com.bewi.stockmanager.position.dto.PositionDTO;
import com.bewi.stockmanager.position.dto.PositionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/*
 PositionController can be used to orchestrate the domain.
 It should not contain business logic and is just a thin mapper
 from REST to the Domain.
 */

@RestController
@RequestMapping("/api/positions/")
@Tag(name = "Position", description = "Operations related to positions")
public class PositionController {

    private PositionRepository positionRepository;
    private PositionMapper positionMapper;

    @Operation(summary = "Returns all Positions")
    @GetMapping
    public ResponseEntity<List<PositionDTO>> getPositions() {
        return ResponseEntity.ok(positionRepository.findAll()
                .stream().map(positionMapper::toDTO).toList());
    }

    @Operation(summary = "Returns a Position by WKN")
    @GetMapping("/{wkn}")
    public ResponseEntity<PositionDTO> getPositionByWkn(@PathVariable String wkn) {
        Optional<Position> byWKN = positionRepository.findByWKN(wkn);
        if (byWKN.isPresent()) {
            PositionDTO dto = positionMapper.toDTO(byWKN.get());
            //TODO
            dto.price = "42.42";
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Creates a Position by id")
    @PostMapping
    public ResponseEntity<PositionDTO> createPosition(@RequestBody PositionDTO position) {
        PositionDTO createdPosition = positionMapper
                .toDTO(positionRepository
                        .save(positionMapper.toDomain(position)));
        return new ResponseEntity<>(createdPosition, HttpStatus.CREATED);
    }

    @Operation(summary = "Updates a Position by id")
    @PatchMapping("/{id}")
    public ResponseEntity<PositionDTO> updatePosition(@PathVariable Long id, @RequestBody PositionDTO positionUpdates) {
        // Position updatedPosition = positionService.updatePosition(id, positionUpdates);
        return ResponseEntity.ok(positionUpdates);
    }
}

