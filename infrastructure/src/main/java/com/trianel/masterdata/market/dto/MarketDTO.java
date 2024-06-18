package com.trianel.masterdata.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MarketDTO {

    @Schema(description = "Unique identifier of the Market", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public UUID id;

    @Schema(description = "Unique name of the Market", requiredMode = Schema.RequiredMode.REQUIRED)
    public String name;
}
