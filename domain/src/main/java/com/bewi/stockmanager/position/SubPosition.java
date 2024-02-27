package com.bewi.stockmanager.position;

import java.time.OffsetDateTime;

public record SubPosition(int strikeprice, int quantity, OffsetDateTime strikeDate) {
}
