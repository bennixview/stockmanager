package com.trianel.masterdata.market.api;


import com.trianel.masterdata.market.Market;
import com.trianel.masterdata.market.MarketRepository;
import com.trianel.masterdata.market.dto.MarketDTO;
import com.trianel.masterdata.market.dto.MarketMapper;
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
import java.util.UUID;

/*
 MarketController can be used to orchestrate the domain.
 It should not contain business logic and is just a thin mapper
 from REST to the Domain.
 */

@RestController
@RequestMapping("/api/markets")
@Tag(name = "Market", description = "Operations related to Markets")
public class MarketController {

    private final MarketRepository marketRepository;
    private final MarketMapper marketMapper;

    public MarketController(MarketRepository marketRepository, MarketMapper marketMapper) {
        this.marketRepository = marketRepository;
        this.marketMapper = marketMapper;
    }

    @Operation(summary = "Returns all Markets")
    @GetMapping
    public ResponseEntity<List<MarketDTO>> getMarkets() {
        return ResponseEntity.ok(marketRepository.findAll().stream().map(marketMapper::toDTO).toList());
    }

    @Operation(summary = "Returns a Market by name")
    @GetMapping("/{name}")
    public ResponseEntity<MarketDTO> getMarketByWkn(@PathVariable String name) {
        Optional<Market> foundMarket = marketRepository.findByName(name);
        if (foundMarket.isPresent()) {
            MarketDTO dto = marketMapper.toDTO(foundMarket.get());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Creates a Market from MarketDTO")
    @PostMapping
    public ResponseEntity<MarketDTO> createMarket(@RequestBody MarketDTO marketDTO) {
        MarketDTO createdMarket = marketMapper.toDTO(marketRepository.save(marketMapper.toDomain(marketDTO)));
        return new ResponseEntity<>(createdMarket, HttpStatus.CREATED);
    }

    @Operation(summary = "Updates a Market by id")
    @PatchMapping("/{id}")
    public ResponseEntity<MarketDTO> updateMarket(@PathVariable UUID id, @RequestBody MarketDTO MarketUpdates) {
        // Market updatedMarket = MarketService.updateMarket(id, MarketUpdates);
        return ResponseEntity.ok(MarketUpdates);
    }
}

