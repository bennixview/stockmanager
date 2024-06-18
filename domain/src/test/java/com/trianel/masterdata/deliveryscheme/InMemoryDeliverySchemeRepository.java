package com.trianel.masterdata.deliveryscheme;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryDeliverySchemeRepository implements DeliverySchemeRepository {

    private final Map<UUID, DeliveryScheme> DeliverySchemeDB = new HashMap<>();


    @Override
    public DeliveryScheme save(DeliveryScheme deliveryScheme) {
        DeliverySchemeDB.put(deliveryScheme.getId(), deliveryScheme);
        return deliveryScheme;
    }

    @Override
    public Collection<DeliveryScheme> findAll() {
        return DeliverySchemeDB.values();
    }

    @Override
    public Optional<DeliveryScheme> findByDeliveryStructure(DeliveryStructure deliveryStructure) {
        return Optional.empty();
    }

    @Override
    public void delete(DeliveryScheme deliveryScheme) {
        DeliverySchemeDB.remove(deliveryScheme.getId());
    }

    @Override
    public Optional<DeliveryScheme> findById(UUID id) {
        return DeliverySchemeDB.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id)).findFirst().map(Map.Entry::getValue);
    }
}
