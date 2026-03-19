package com.cmanager.app.application.controller;

import com.cmanager.app.application.data.ShowCreateRequest;
import com.cmanager.app.application.data.ShowDTO;
import com.cmanager.app.application.service.ShowService;
import com.cmanager.app.core.data.PageResultResponse;
import com.cmanager.app.core.utils.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/shows")
@Tag(name = "ShowController", description = "API de gerenciamento de TV Shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @Operation(
            summary = "sync",
            description = "Sincroniza um show da API TVMaze e persiste no banco (apenas ADMIN)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Show sincronizado com sucesso"),
                    @ApiResponse(responseCode = "409", description = "Show já existe"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado — requer perfil ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Show não encontrado no TVMaze")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowDTO> sync(@RequestBody @Valid ShowCreateRequest req) {
        final ShowDTO dto = ShowDTO.convertEntity(showService.sync(req.name()));
        return ResponseEntity.created(URI.create("/api/shows/" + dto.id())).body(dto);
    }

    @Operation(
            summary = "list",
            description = "Lista shows com paginação, filtro por nome e ordenação",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<PageResultResponse<ShowDTO>> list(
            @Parameter(description = "Filtro por nome (parcial, case-insensitive)", example = "breaking")
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação", example = "name")
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(value = "sortOrder", defaultValue = "ASC") String sortOrder
    ) {
        final var pageable = Util.getPageable(page, size, sortField, sortOrder);
        final var pageShows = showService.list(name, pageable);
        final var result = new PageImpl<>(
                pageShows.getContent().stream().map(ShowDTO::convertEntity).toList(),
                pageable,
                pageShows.getTotalElements()
        );
        return ResponseEntity.ok(PageResultResponse.from(result));
    }
}
