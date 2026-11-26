package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarUsuarioUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.UsuarioDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-23: Gestión de usuarios (exclusivo de ADMIN).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final GestionarUsuarioUseCase gestionarUsuario;

    public UsuarioController(GestionarUsuarioUseCase gestionarUsuario) {
        this.gestionarUsuario = gestionarUsuario;
    }

    @GetMapping
    public List<UsuarioResponse> listar(UsuarioSesion actor) {
        return gestionarUsuario.listar(actor).stream().map(UsuarioResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(UsuarioSesion actor, @Valid @RequestBody CrearUsuarioRequest body) {
        return UsuarioResponse.de(gestionarUsuario.crear(
                actor, body.nombreCompleto(), body.usuario(), body.clave(), body.idRol()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                           @Valid @RequestBody ActualizarUsuarioRequest body) {
        gestionarUsuario.actualizar(actor, id, body.nombreCompleto(), body.idRol());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(UsuarioSesion actor, @PathVariable Integer id,
                                              @Valid @RequestBody CambiarEstadoRequest body) {
        gestionarUsuario.cambiarEstado(actor, id, body.activo());
        return ResponseEntity.noContent().build();
    }
}
