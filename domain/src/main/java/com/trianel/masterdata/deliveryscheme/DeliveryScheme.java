package com.trianel.masterdata.deliveryscheme;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.TimeZone;
import java.util.UUID;

@AggregateRoot
@EqualsAndHashCode
@Getter
public class DeliveryScheme {
    private final UUID id;
    private final String name;
    private final TimeZone timeZone;
    private final DeliveryStructure deliveryStructure;

    @Builder
    public DeliveryScheme(UUID id, String name, TimeZone timeZone, DeliveryStructure deliveryStructure) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = name;
        this.timeZone = timeZone;
        this.deliveryStructure = deliveryStructure;
    }
}
