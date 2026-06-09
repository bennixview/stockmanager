package com.bewi.stockmanager.controller.ui;

import com.bewi.paging.Page;
import com.bewi.paging.Paged;
import com.bewi.paging.Paging;
import com.bewi.stockmanager.position.Position;
import com.bewi.stockmanager.position.PositionRepository;
import com.bewi.stockmanager.position.PositionService;
import com.bewi.stockmanager.position.dto.OrderDTO;
import com.bewi.stockmanager.position.dto.PositionDTO;
import com.bewi.stockmanager.position.dto.PositionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/positions/")
public class PositionWebController {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final PositionService positionService;


    @Autowired
    public PositionWebController(PositionRepository positionRepository,
                                 PositionMapper positionMapper,
                                 PositionService positionService) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
        this.positionService = positionService;
    }

    @PostMapping("/open/{id}")
    public String savePosition(OrderDTO order, Model model) {

        Position position = positionService.openPosition(order.positionId, order.name, order.wkn, order.isin,
                order.strikeprice, order.quantity, order.price, order.strikeDate);
        model.addAttribute("position", position);
        return "position/details";
    }

/*    @GetMapping("/open/")
    public String openPosition(*//*Model model*//*) {
        //model.addAttribute("order", new OrderDTO());
         return "redirect:/open/1";
    }*/

    @GetMapping("/open/{id}")
    public String openPositionById(@PathVariable String id, Model model) {
        Optional<Position> positions = id.equals("new") ? Optional.empty() : positionRepository.findById(UUID.fromString(id));
        if (positions.isEmpty()) {
            model.addAttribute("order", new OrderDTO());
            return "position/open";
        }
        PositionDTO positionDTO = positionMapper.toDTO(positions.get());
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPositionId(positionDTO.id);
        orderDTO.setName(positionDTO.name);
        orderDTO.setPrice(positionDTO.price);
        orderDTO.setQuantity(positionDTO.quantity);
        orderDTO.setIsin(positionDTO.isin);
        orderDTO.setWkn(positionDTO.wkn);
        orderDTO.setStrikeprice(positionDTO.strikeprice);
        model.addAttribute("order", orderDTO);
        return "position/open";
    }

    @GetMapping
    public String list(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                       @RequestParam(value = "size", required = false, defaultValue = "5") int size, Model model) {
        model.addAttribute("positions", page(pageNumber, size));
        return "position/list";
    }

    private Paged<Position> page(int pageNumber, int size) {
        List<Position> all = positionRepository.findAll().stream()
                .sorted(Comparator.comparing(Position::getName))
                .toList();
        int totalPages = Math.max(1, (all.size() + size - 1) / size);
        int currentPage = Math.min(Math.max(pageNumber, 1), totalPages);
        List<Position> content = all.stream()
                .skip((long) (currentPage - 1) * size)
                .limit(size)
                .toList();
        return new Paged<>(new Page<>(content, totalPages), Paging.of(totalPages, currentPage, size));
    }

    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model) {
        var positions = positionRepository.findById(UUID.fromString(id));
        if (positions.isEmpty()) {
            return "position/list";
        }
        model.addAttribute("position", positions.get());
        return "position/details";
    }

}
