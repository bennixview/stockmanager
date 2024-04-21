package com.bewi.stockmanager.position;

import java.time.OffsetDateTime;

public record Tranche(int strikeprice, int quantity, OffsetDateTime strikeDate) {
}
