package com.trianel.masterdata.market;

import org.jmolecules.ddd.annotation.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketRepository {

    /**
     * Persits the {@link Market}
     *
     * @param Market is the {@link Market} to save
     * @return The persited {@link Market}
     */
    Market save(final Market Market);

    /**
     * Method for request all available {@link Market}
     *
     * @return Collection of all available {@link Market}
     */
    Collection<Market> findAll();

    /**
     * Method requests a secific {@link Market} identified by then {@name}
     *
     * @param name
     * @return The {@link Market} identified by then {@name} if available, otherwise null
     */
    Optional<Market> findByName(String name);

    /**
     * Method requests a secific {@link Market} identified by then {@id}
     *
     * @param id
     * @return The {@link Market} identified by then {@id} if available, otherwise null
     */
    Optional<Market> findById(UUID id);

    /**
     * Delets the {@link Market}
     */
    void delete(final Market Market);

}
