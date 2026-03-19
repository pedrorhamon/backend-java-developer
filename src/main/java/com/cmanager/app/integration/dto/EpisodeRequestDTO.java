package com.cmanager.app.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "EpisodeRequestDTO", description = "Objeto da representação de Episódio da API externa")
@JsonIgnoreProperties(ignoreUnknown = true)
public record EpisodeRequestDTO(
        @JsonProperty("id")
        @Schema(name = "id", description = "Id")
        Integer id,
        @JsonProperty("name")
        @Schema(name = "name", description = "Nome")
        String name,
        @JsonProperty("season")
        @Schema(name = "season", description = "Temporada")
        Integer season,
        @JsonProperty("number")
        @Schema(name = "number", description = "Episódio")
        Integer number,
        @JsonProperty("type")
        @Schema(name = "type", description = "Tipo")
        String type,
        @JsonProperty("airdate")
        @Schema(name = "airdate", description = "Lançamento")
        String airdate,
        @JsonProperty("airtime")
        @Schema(name = "airtime", description = "Hora de lançamento")
        String airtime,
        @JsonProperty("airstamp")
        @Schema(name = "airstamp", description = "Airstamp")
        OffsetDateTime airstamp,
        @JsonProperty("runtime")
        @Schema(name = "runtime", description = "Duração")
        Integer runtime,
        @JsonProperty("rating")
        @Schema(name = "rating", description = "Nota")
        RatingDTO rating,
        @JsonProperty("summary")
        @Schema(name = "summary", description = "Resumo")
        String summary
) {
}
