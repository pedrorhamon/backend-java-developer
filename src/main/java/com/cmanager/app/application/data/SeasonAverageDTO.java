package com.cmanager.app.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "SeasonAverageDTO", description = "Média de rating por temporada")
public record SeasonAverageDTO(

        @JsonProperty("season")
        @Schema(name = "season", description = "Número da temporada")
        Integer season,

        @JsonProperty("averageRating")
        @Schema(name = "averageRating", description = "Média de rating da temporada (0 se todos os ratings forem nulos)")
        BigDecimal averageRating

) {
}
