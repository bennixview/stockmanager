package com.bewi.stockmanager.position.persitence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;


@Document
public class PositionDoc {
    @Id
    public String id;
    public String name;
    public String wkn;
    public String isin;
    public Set<TrancheDoc> tranches;
    public int strikeprice;
    public int quantity;

}
