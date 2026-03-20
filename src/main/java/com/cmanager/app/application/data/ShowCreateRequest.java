package com.cmanager.app.application.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ShowCreateRequest", description = "Payload para sincronizacao de um show")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShowCreateRequest(
        @NotBlank
        @Size(min = 1, max = 265)
        @Schema(name = "name", description = "Nome do show a ser sincronizado com o TVMaze")
        String name
) {
}
