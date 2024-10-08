package com.bewi.stockmanager.position.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
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
