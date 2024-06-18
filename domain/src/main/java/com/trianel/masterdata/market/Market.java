package com.trianel.masterdata.market;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.UUID;

@AggregateRoot
@EqualsAndHashCode
@Getter
public class Market {
    private final UUID id;
    private String name;

    @Builder
    public Market(UUID id, String name) {
        //TODO ID / Name = ValueObject?
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = name;
    }

    public void updateNameIfAllowed(String name) {

        if(1==1){
            this.name = name;
        }

    }
}
