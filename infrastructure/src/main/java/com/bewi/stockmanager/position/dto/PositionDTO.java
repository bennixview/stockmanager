package com.bewi.stockmanager.position.dto;

import java.util.Set;

public class PositionDTO {
    public String id;
    public String name;
    public String wkn;
    public String isin;
    public Set<TrancheDTO> tranches;
    public int strikeprice;
    public int quantity;
    public String price;

}
