package com.bewi.stockmanager.controller;


import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@RestController
@RequestMapping("/api/positions/")
@Tag(name = "Position", description = "Operations related to positions")
public class PositionController {

    @Autowired
    private PositionRepository positionRepository;

    @Operation(summary = "Returns all Positions")
    @GetMapping
    public ResponseEntity<List<Position>> getPositions() {
        return ResponseEntity.ok(positionRepository.findAll().stream().toList());
    }

    @Operation(summary = "Creates a Position by id")
    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position createdPosition = positionRepository.save(position);
        return new ResponseEntity<>(createdPosition, HttpStatus.CREATED);
    }

    @Operation(summary = "Updates a Position by id")
    @PatchMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, @RequestBody Position positionUpdates) {
       // Position updatedPosition = positionService.updatePosition(id, positionUpdates);
        return ResponseEntity.ok(positionUpdates);
    }
}

