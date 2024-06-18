package com.trianel.masterdata.market.persistence;

import com.trianel.masterdata.paging.Page;
import com.trianel.masterdata.paging.Paged;
import com.trianel.masterdata.paging.Paging;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trianel.masterdata.market.Market;
import com.trianel.masterdata.market.MarketRepository;
import com.trianel.masterdata.market.dto.MarketDTO;
import com.trianel.masterdata.market.dto.MarketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is just an example of a Repository that uses for now a json file instead of database.
 * If you want to use JPA or MongoDB Repository it is a good approach to delegate to a seperate
 * Spring interface repository as DAO
 */


@Component
@RequiredArgsConstructor
public class MarketRepositoryImpl implements MarketRepository {

    private final Map<UUID, Market> markets = new HashMap<>();
    private final MarketMapper marketMapper;
    private static final String filePath = "Markets.json";

    private Map<UUID, Market> getMarkets() {
        if (markets.isEmpty()) {
            readMarketsFromFile(filePath).forEach(marketDTO -> {
                Market market = marketMapper.toDomain(marketDTO);
                markets.put(market.getId(), market);
            });
        }
        return markets;
    }


    @Override
    public Market save(Market market) {
        getMarkets().put(market.getId(), market);
        writeMarketsToFile(getMarkets().values().stream().map(marketMapper::toDTO).toList());
        System.out.println("Updated Markets written back to file.");
        return market;
    }

    @Override
    public Collection<Market> findAll() {
        return getMarkets().values().stream().sorted(Comparator.comparing(Market::getName)).toList();
    }


    @Override
    public Optional<Market> findByName(String name) {
        return markets.values().stream().filter(market -> market.getName().equals(name)).findFirst();
    }


    @Override
    public void delete(Market Market) {
        markets.remove(Market.getId());
    }

    @Override
    public Optional<Market> findById(UUID id) {
        return markets.values().stream().filter(Market -> Market.getId().equals(id)).findFirst();
    }

    public Paged<Market> getMarkets(int pageNumber, int size) {
        Collection<Market> allMarkets = this.findAll();
        List<Market> paged = allMarkets.stream()
                .skip(pageNumber)
                .limit(size)
                .collect(Collectors.toList());
        int totalPages = (allMarkets.size() + size - 1) / size;
        return new Paged<>(new Page<>(paged, totalPages), Paging.of(totalPages, pageNumber, size));
    }

    public static List<MarketDTO> readMarketsFromFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(); // Return an empty list in case of error
        }
    }

    private static void writeMarketsToFile(List<MarketDTO> Markets) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ignore null fields
        try {
            objectMapper.writeValue(new File(filePath), Markets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
