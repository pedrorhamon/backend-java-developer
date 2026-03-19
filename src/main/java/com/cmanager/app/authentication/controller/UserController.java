package com.cmanager.app.authentication.controller;

import com.cmanager.app.authentication.data.UserCreateRequest;
import com.cmanager.app.authentication.data.UserDTO;
import com.cmanager.app.authentication.data.UserUpdateRequest;
import com.cmanager.app.authentication.service.UserService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "UserController",
        description = "API de gerenciamento de usuários"
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResultResponse<UserDTO>> list(
            @Parameter(description = "Nome do usuário para filtro", example = "admin")
            @RequestParam(name = "username", required = false, defaultValue = "") String username,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação", example = "username")
            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(value = "sortOrder", defaultValue = "ASC") String sortOrder
    ) {
        final var pageable = Util.getPageable(page, size, sortField, sortOrder);
        final var listUsers = userService.findByUsernameContainingIgnoreCase(username, pageable);
        final var pageProducts = new PageImpl<>(
                listUsers.stream()
                        .map(UserDTO::convertEntity)
                        .toList(),
                pageable,
                listUsers.getTotalElements()
        );
        return ResponseEntity.ok(PageResultResponse.from(pageProducts));
    }


    @Operation(
            summary = "get",
            description = "Consulta um usuário pelo id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<UserDTO> get(@PathVariable String id) {
        final UserDTO dto = UserDTO.convertEntity(userService.findById(id));
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "create",
            description = "Registra um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> create(@RequestBody @Valid UserCreateRequest req) {
        final UserDTO dto = UserDTO.convertEntity(userService.create(req));
        return ResponseEntity.created(URI.create("/api/users/" + dto.id())).body(dto);
    }

    @Operation(
            summary = "update",
            description = "Atualiza um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> update(@PathVariable String id,
                                          @RequestBody @Valid UserUpdateRequest req) {
        final UserDTO dto = UserDTO.convertEntity(userService.update(id, req));
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "delete",
            description = "Remove um usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário removido com sucesso")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
