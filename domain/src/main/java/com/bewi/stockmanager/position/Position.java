package com.bewi.stockmanager.position;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private Set<SubPosition> subPositions = new HashSet<>();


    @Builder
    public Position(@Nonnull final String name, final int strikeprice, final int quantity, final OffsetDateTime strikeDate) {
        this.id = UUID.randomUUID();
        this.name = name;


        subPositions.add(
                new SubPosition(strikeprice, quantity, strikeDate));

    }

    public int getStrikeprice() {
        return subPositions.stream().mapToInt(SubPosition::strikeprice).sum();
    }

    public int getQuantity() {
        return subPositions.stream().mapToInt(SubPosition::quantity).sum();
    }




}
