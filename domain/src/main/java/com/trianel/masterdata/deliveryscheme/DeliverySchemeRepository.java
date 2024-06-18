package com.trianel.masterdata.deliveryscheme;

import org.jmolecules.ddd.annotation.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliverySchemeRepository {

    /**
     * Persits the {@link DeliveryScheme}
     *
     * @param DeliveryScheme is the {@link DeliveryScheme} to save
     * @return The persited {@link DeliveryScheme}
     */
    DeliveryScheme save(final DeliveryScheme DeliveryScheme);

    /**
     * Method for request all available {@link DeliveryScheme}
     *
     * @return Collection of all available {@link DeliveryScheme}
     */
    Collection<DeliveryScheme> findAll();

    /**
     * Method requests a secific {@link DeliveryScheme} identified by then {@DeliveryStructure}
     *
     * @param @DeliveryStructure
     * @return The {@link DeliveryScheme} identified by then {@DeliveryStructure} if available, otherwise null
     */
    Optional<DeliveryScheme> findByDeliveryStructure(DeliveryStructure deliveryStructure);

    /**
     * Method requests a secific {@link DeliveryScheme} identified by then {@id}
     *
     * @param id
     * @return The {@link DeliveryScheme} identified by then {@id} if available, otherwise null
     */
    Optional<DeliveryScheme> findById(UUID id);

    /**
     * Delets the {@link DeliveryScheme}
     */
    void delete(final DeliveryScheme DeliveryScheme);

}
