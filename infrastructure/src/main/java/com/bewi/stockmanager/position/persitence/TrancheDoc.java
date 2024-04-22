package com.bewi.stockmanager.position.persitence;

import java.time.OffsetDateTime;

public class TrancheDoc {
    public int strikeprice;
    public int quantity;
    public OffsetDateTime strikeDate; // Using String for simplicity; consider using a date type
}
