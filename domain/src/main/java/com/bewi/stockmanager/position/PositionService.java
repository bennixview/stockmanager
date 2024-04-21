package com.bewi.stockmanager.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;


    public Position openPosition(Position position) {


        Optional<Position> foundPosition = this.positionRepository.findByWKN(position.getWkn());
        if (foundPosition.isPresent()) {
            Position addTo = foundPosition.get();
            for (Tranche tranche : position.getTranches()) {
                addTo.addTranche(tranche);
            }
            return this.positionRepository.save(addTo);
        } else {
            return this.positionRepository.save(position);
        }
    }
}
