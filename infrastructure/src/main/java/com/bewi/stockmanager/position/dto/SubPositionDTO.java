package com.bewi.stockmanager.position.dto;

import java.time.OffsetDateTime;

public class SubPositionDTO {
    public int strikeprice;
    public int quantity;
    public OffsetDateTime strikeDate; // Using String for simplicity; consider using a date type


}
