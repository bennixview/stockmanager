package com.bewi.stockmanager.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

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


    public Position openPosition(String positionId,
                                 String name,
                                 String wkn,
                                 String isin,
                                 int strikeprice,
                                 int quantity,
                                 String price,
                                 OffsetDateTime strikeDate) {


        Optional<Position> foundPosition = this.positionRepository.findByWKN(wkn);
        if (foundPosition.isPresent()) {
            Position addTo = foundPosition.get();
            addTo.addTranche(new Tranche(strikeprice, quantity, strikeDate));
            return this.positionRepository.save(addTo);
        } else {


            return this.positionRepository.save(Position.builder()
                    .id(UUID.fromString(positionId))
                    .name(name)
                    .quantity(quantity)
                    .wkn(wkn)
                    .strikeprice(strikeprice)
                    .isin(isin)
                    .strikeDate(strikeDate)
                    .build());
        }
    }
}
