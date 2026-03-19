package com.cmanager.app.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "SeasonAverageDTO", description = "Media de rating por temporada")
public record SeasonAverageDTO(
        @JsonProperty("season")
        @Schema(description = "Numero da temporada")
        Integer season,
        @JsonProperty("averageRating")
        @Schema(description = "Media de rating da temporada")
        BigDecimal averageRating
) {
}
