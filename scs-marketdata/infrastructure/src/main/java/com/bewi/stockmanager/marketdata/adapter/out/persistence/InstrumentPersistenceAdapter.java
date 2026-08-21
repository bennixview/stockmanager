package com.bewi.stockmanager.marketdata.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bewi.stockmanager.marketdata.domain.Instrument;
import com.bewi.stockmanager.marketdata.domain.InstrumentRepository;
import com.bewi.stockmanager.marketdata.domain.Isin;
import com.bewi.stockmanager.marketdata.domain.Symbol;
import com.bewi.stockmanager.marketdata.domain.Wkn;

/** Driven adapter implementing the domain's {@link InstrumentRepository} on top of JPA. */
@Component
@Transactional(readOnly = true)
class InstrumentPersistenceAdapter implements InstrumentRepository {

    private final SpringDataInstrumentRepository repository;

    InstrumentPersistenceAdapter(SpringDataInstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Instrument save(Instrument instrument) {
        repository.save(InstrumentMapper.toEntity(instrument));
        return instrument;
    }

    @Override
    public Optional<Instrument> findBySymbol(Symbol symbol) {
        return repository.findById(symbol.value()).map(InstrumentMapper::toDomain);
    }

    @Override
    public Optional<Instrument> findByIsin(Isin isin) {
        return repository.findByIsin(isin.value()).map(InstrumentMapper::toDomain);
    }

    @Override
    public Optional<Instrument> findByWkn(Wkn wkn) {
        return repository.findByWkn(wkn.value()).map(InstrumentMapper::toDomain);
    }

    @Override
    public List<Instrument> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return repository.findAll(Sort.by("name")).stream().limit(limit).map(InstrumentMapper::toDomain).toList();
        }
        return repository.search(query.trim(), Limit.of(limit)).stream().map(InstrumentMapper::toDomain).toList();
    }

    @Override
    public List<Instrument> findAll() {
        return repository.findAll(Sort.by("name")).stream().map(InstrumentMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void delete(Symbol symbol) {
        repository.deleteById(symbol.value());
    }
}
