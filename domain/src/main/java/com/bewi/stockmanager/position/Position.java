package com.bewi.stockmanager.position;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class Position {

    private final UUID id;
    private final String name;

    //private final int quantity;
    private Set<SubPosition> subPositions = new HashSet<>();

    public Position(String name, int strikeprice, int quantity) {
        this.id = UUID.randomUUID();
        this.name = name;


        subPositions.add(
                new SubPosition(strikeprice, quantity, OffsetDateTime.now()));

    }

    public int getStrikeprice() {
        return 1; //subPositions.stream().mapToInt(SubPosition::getStrikeprice).sum();
    }




}
