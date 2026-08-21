package com.bewi.stockmanager.marketdata.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataInstrumentRepository extends JpaRepository<InstrumentJpaEntity, String> {

    Optional<InstrumentJpaEntity> findByIsin(String isin);

    Optional<InstrumentJpaEntity> findByWkn(String wkn);

    @Query("""
            select i from InstrumentJpaEntity i
            where lower(i.symbol) like lower(concat('%', :query, '%'))
               or lower(i.name)   like lower(concat('%', :query, '%'))
               or lower(i.isin)   like lower(concat('%', :query, '%'))
               or lower(i.wkn)    like lower(concat('%', :query, '%'))
            order by
               case when lower(i.symbol) = lower(:query) then 0 else 1 end,
               i.name
            """)
    List<InstrumentJpaEntity> search(@Param("query") String query, Limit limit);
}
