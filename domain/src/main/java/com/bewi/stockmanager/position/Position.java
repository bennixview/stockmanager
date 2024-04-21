package com.bewi.stockmanager.position;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

@EqualsAndHashCode
@AllArgsConstructor
public class Position {
    @Getter
    private final UUID id;
    @Getter
    private final String name;
    @Getter
    private final String wkn;
    @Getter
    private final String isin;
    private Set<Tranche> tranches = new HashSet<>();


    @Builder
    public Position(UUID id, @Nonnull final String name, final int strikeprice, final int quantity, final OffsetDateTime strikeDate, @Nonnull final String wkn, final String isin, Set<Tranche> tranches) {
        this.id = requireNonNullElse(id, UUID.randomUUID());
        this.name = name;
        this.wkn = wkn;
        this.isin = isin;
        this.addTranche(strikeprice, quantity, strikeDate);
        this.addAllTranche(tranches);
    }

    private void addTranche(int strikeprice, int quantity, OffsetDateTime strikeDate) {
        this.addTranche(new Tranche(strikeprice, quantity, strikeDate == null ? OffsetDateTime.now() : strikeDate));
    }


    public void addAllTranche(Set<Tranche> tranches) {
        if (tranches == null) return;
        tranches.forEach(this::addTranche);
    }

    public void addTranche(Tranche tranche) {
        if (tranche.quantity() != 0 && tranche.strikeprice() != 0) tranches.add(tranche);
    }

    public int getStrikeprice() {
        return tranches.stream().mapToInt(Tranche::strikeprice).sum();
    }

    public int getQuantity() {
        return tranches.stream().mapToInt(Tranche::quantity).sum();
    }

    public Set<Tranche> getTranches() {
        return Collections.unmodifiableSet(tranches);
    }
}
