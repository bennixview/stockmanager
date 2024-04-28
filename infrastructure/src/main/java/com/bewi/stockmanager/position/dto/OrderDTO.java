package com.bewi.stockmanager.position.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
public class OrderDTO {
    public String positionId;
    public String name;
    public String wkn;
    public String isin;
    public int strikeprice;
    public int quantity;
    public String price;
    public OffsetDateTime strikeDate;
}
