package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.ActualizarUsuario;
import com.sigoc.sigoc_web.aplication.usecases.CambiarEstadoUsuario;
import com.sigoc.sigoc_web.aplication.usecases.CrearUsuario;
import com.sigoc.sigoc_web.aplication.usecases.ListarUsuarios;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.UsuarioDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-02: gestión de usuarios (solo Administrador).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final ListarUsuarios listarUsuarios;
    private final CrearUsuario crearUsuario;
    private final ActualizarUsuario actualizarUsuario;
    private final CambiarEstadoUsuario cambiarEstadoUsuario;

    public UsuarioController(ListarUsuarios listarUsuarios, CrearUsuario crearUsuario,
                             ActualizarUsuario actualizarUsuario, CambiarEstadoUsuario cambiarEstadoUsuario) {
        this.listarUsuarios = listarUsuarios;
        this.crearUsuario = crearUsuario;
        this.actualizarUsuario = actualizarUsuario;
        this.cambiarEstadoUsuario = cambiarEstadoUsuario;
    }

    @GetMapping
    public List<UsuarioResponse> listar(UsuarioSesion actor) {
        return listarUsuarios.ejecutar(actor).stream().map(UsuarioResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(UsuarioSesion actor, @Valid @RequestBody CrearUsuarioRequest body) {
        return UsuarioResponse.de(crearUsuario.ejecutar(actor, body.nombreCompleto(), body.usuario(),
                body.clave(), body.idRol()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                           @Valid @RequestBody ActualizarUsuarioRequest body) {
        actualizarUsuario.ejecutar(actor, id, body.nombreCompleto(), body.idRol());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(UsuarioSesion actor, @PathVariable Integer id,
                                              @Valid @RequestBody CambiarEstadoRequest body) {
        cambiarEstadoUsuario.ejecutar(actor, id, body.activo());
        return ResponseEntity.noContent().build();
    }
}
