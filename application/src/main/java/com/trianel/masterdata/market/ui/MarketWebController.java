package com.trianel.masterdata.market.ui;

import com.trianel.masterdata.market.Market;
import com.trianel.masterdata.market.dto.MarketDTO;
import com.trianel.masterdata.market.dto.MarketMapper;
import com.trianel.masterdata.market.persistence.MarketRepositoryImpl;
import com.trianel.masterdata.paging.Paged;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/markets")
public class MarketWebController {

    private final MarketRepositoryImpl marketRepository;
    private final MarketMapper marketMapper;

    @Autowired
    public MarketWebController(MarketRepositoryImpl marketRepository,
                               MarketMapper marketMapper) {
        this.marketRepository = marketRepository;
        this.marketMapper = marketMapper;
    }

    @GetMapping
    public String list(@RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                       @RequestParam(value = "size", required = false, defaultValue = "5") int size, Model model) {
        Paged<Market> markets = marketRepository.getMarkets(pageNumber, size);
        model.addAttribute("markets", markets);
        return "market/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable String id, Model model) {
        Optional<Market> market = id.equals("new") ? Optional.empty() : marketRepository.findById(UUID.fromString(id));
        if (market.isEmpty()) {
            model.addAttribute("market", new MarketDTO());
            return "market/edit";
        }
        MarketDTO marketDTO = marketMapper.toDTO(market.get());
        model.addAttribute("market", marketDTO);
        return "market/edit";
    }

    @PostMapping("/edit/{id}")
    public String saveMarket(MarketDTO marketDTO, Model model) {

        Market market = marketRepository.save(marketMapper.toDomain(marketDTO));
        model.addAttribute("position", market);
        return "market/details";
    }
}
