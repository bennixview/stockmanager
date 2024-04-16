package com.bewi.stockmanager.controller;

import com.bewi.paging.Page;
import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions/")
public class PositionController {

    @Autowired
    private PositionRepository positionRepository;


    @GetMapping
    public ResponseEntity<List<Position>> getPositions() {
        ;
        return ResponseEntity.ok(positionRepository.findAll().stream().toList());
    }



    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody Position position) {
        Position createdPosition = positionRepository.save(position);
        return new ResponseEntity<>(createdPosition, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, @RequestBody Position positionUpdates) {
       // Position updatedPosition = positionService.updatePosition(id, positionUpdates);
        return ResponseEntity.ok(positionUpdates);
    }
}

