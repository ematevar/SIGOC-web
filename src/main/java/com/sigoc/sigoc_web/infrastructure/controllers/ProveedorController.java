package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarProveedorUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ProveedorDtos.GuardarProveedorRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ProveedorDtos.ProveedorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-09: Gestión de proveedores.
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final GestionarProveedorUseCase gestionarProveedor;

    public ProveedorController(GestionarProveedorUseCase gestionarProveedor) {
        this.gestionarProveedor = gestionarProveedor;
    }

    @GetMapping
    public List<ProveedorResponse> listar(UsuarioSesion actor) {
        return gestionarProveedor.listar(actor).stream().map(ProveedorResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarProveedorRequest body) {
        return ProveedorResponse.de(gestionarProveedor.guardar(
                actor, null, body.razonSocial(), body.ruc(), body.telefono(), body.direccion()));
    }

    @PutMapping("/{id}")
    public ProveedorResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                        @Valid @RequestBody GuardarProveedorRequest body) {
        return ProveedorResponse.de(gestionarProveedor.guardar(
                actor, id, body.razonSocial(), body.ruc(), body.telefono(), body.direccion()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarProveedor.eliminar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
