package com.bewi.stockmanager.position;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

//TODO Marker Annotation Repository jmolecules
public interface PositionRepository {

    /**
     * Persits the {@link Position}
     *
     * @param Position is the {@link Position} to save
     * @return The persited {@link Position}
     */
    Position save(final Position Position);

    /**
     * Method for request all available {@link Position}
     *
     * @return Collection of all available {@link Position}
     */
    Collection<Position> findAll();

    /**
     * Method requests a secific {@link Position} identified by then {@wkn}
     *
     * @param wkn
     * @return The {@link Position} identified by then {@wkn} if available, otherwise null
     */
    Optional<Position> findByWKN(String wkn);

    /**
     * Method requests a secific {@link Position} identified by then {@id}
     * @param id
     * @return The {@link Position} identified by then {@id} if available, otherwise null
     */
    Optional<Position> findById(UUID id);

    /**
     * Delets the {@link Position}
     */
    void delete(final Position Position);

}
