package com.bewi.stockmanager.position;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPositionRepository implements PositionRepository {

    private final Map<String, Position> positionDB = new HashMap<>();


    @Override
    public Position save(Position position) {
        positionDB.put(position.getWkn(), position);
        return position;
    }

    @Override
    public Collection<Position> findAll() {
        return positionDB.values();
    }

    @Override
    public Optional<Position> findByWKN(String wkn) {
        return Optional.ofNullable(positionDB.get(wkn));
    }

    @Override
    public void delete(Position position) {
        positionDB.remove(position.getWkn());
    }

    @Override
    public Optional<Position> findById(UUID id) {
        return positionDB.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id)).findFirst().map(Map.Entry::getValue);
    }
}
