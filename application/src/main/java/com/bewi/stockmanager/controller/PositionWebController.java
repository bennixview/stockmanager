package com.bewi.stockmanager.controller;

import com.bewi.paging.Paged;
import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.persitence.PositionRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/positions/")
public class PositionWebController {

    private final PositionRepositoryImpl positionRepository;


    @Autowired
    public PositionWebController(PositionRepositoryImpl positionRepository) {
        this.positionRepository = positionRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                       @RequestParam(value = "size", required = false, defaultValue = "5") int size, Model model) {
        Paged<Position> positions = positionRepository.getPositions(pageNumber, size);
        model.addAttribute("positions", positions);
        return "positions";
    }

}
