package com.trianel.masterdata.market;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryMarketRepository implements MarketRepository {

    private final Map<UUID, Market> marketDB = new HashMap<>();


    @Override
    public Market save(Market market) {
        marketDB.put(market.getId(), market);
        return market;
    }

    @Override
    public Collection<Market> findAll() {
        return marketDB.values();
    }

    @Override
    public Optional<Market> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public void delete(Market market) {
        marketDB.remove(market.getId());
    }

    @Override
    public Optional<Market> findById(UUID id) {
        return marketDB.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id)).findFirst().map(Map.Entry::getValue);
    }
}
