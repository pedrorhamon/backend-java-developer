package com.cmanager.app.application.data;

import com.cmanager.app.application.domain.Show;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ShowDTO", description = "Response de TV show")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShowDTO(
        @JsonProperty("id")
        @Schema(description = "Id interno do show")
        String id,
        @JsonProperty("name")
        @Schema(description = "Nome do show")
        String name,
        @JsonProperty("type")
        @Schema(description = "Tipo (Scripted, Animation...)")
        String type,
        @JsonProperty("language")
        @Schema(description = "Idioma")
        String language,
        @JsonProperty("status")
        @Schema(description = "Status (Running, Ended...)")
        String status,
        @JsonProperty("runtime")
        @Schema(description = "Duracao em minutos")
        Integer runtime,
        @JsonProperty("averageRuntime")
        @Schema(description = "Duracao media em minutos")
        Integer averageRuntime,
        @JsonProperty("officialSite")
        @Schema(description = "Site oficial")
        String officialSite,
        @JsonProperty("rating")
        @Schema(description = "Nota media do show")
        BigDecimal rating,
        @JsonProperty("summary")
        @Schema(description = "Resumo")
        String summary
) {
    public static ShowDTO convertEntity(Show s) {
        return new ShowDTO(
                s.getId(),
                s.getName(),
                s.getType(),
                s.getLanguage(),
                s.getStatus(),
                s.getRuntime(),
                s.getAverageRuntime(),
                s.getOfficialSite(),
                s.getRating(),
                s.getSummary()
        );
    }
}
