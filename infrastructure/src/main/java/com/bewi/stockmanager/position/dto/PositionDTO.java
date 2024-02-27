package com.bewi.stockmanager.position.dto;

import java.util.List;

public class PositionDTO {
    public String id;
    public String name;
    public List<SubPositionDTO> subPositions;
    public int strikeprice;

}
