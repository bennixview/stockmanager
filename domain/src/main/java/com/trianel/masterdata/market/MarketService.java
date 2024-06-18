package com.trianel.masterdata.market;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class MarketService {

   private final MarketRepository marketRepository;


    public Market createOrUpdateMarket(Market market) {
        Optional<Market> byId = marketRepository.findById(market.getId());
        if(byId.isPresent()) {
            Market market1 = byId.get();
            market1.updateNameIfAllowed(market.getName());
            return marketRepository.save(market1);
        } else {
            return marketRepository.save(market);
        }


        //TODO implement
    }

}
