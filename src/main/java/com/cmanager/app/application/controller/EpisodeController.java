package com.cmanager.app.application.controller;

import com.cmanager.app.application.data.SeasonAverageDTO;
import com.cmanager.app.application.service.EpisodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/episodes")
@Tag(name = "EpisodeController", description = "API de episódios e estatísticas")
public class EpisodeController {

    private final EpisodeService episodeService;

    public EpisodeController(EpisodeService episodeService) {
        this.episodeService = episodeService;
    }

    @Operation(
            summary = "average",
            description = "Retorna a média de rating por temporada de um show. "
                    + "Ratings nulos são ignorados. Se todos forem nulos, retorna 0 para a temporada.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Médias calculadas com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Nenhum episódio encontrado para o show")
            }
    )
    @GetMapping("/average")
    public ResponseEntity<List<SeasonAverageDTO>> average(
            @Parameter(description = "ID interno do show", required = true, example = "a1b2c3d4-...")
            @RequestParam("showId") String showId
    ) {
        return ResponseEntity.ok(episodeService.getAverageByShow(showId));
    }
}
