package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.EliminarProveedor;
import com.sigoc.sigoc_web.application.usecases.GuardarProveedor;
import com.sigoc.sigoc_web.application.usecases.ListarProveedores;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ProveedorDtos.GuardarProveedorRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ProveedorDtos.ProveedorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-09: gestión de proveedores (escritura solo Administrador).
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ListarProveedores listarProveedores;
    private final GuardarProveedor guardarProveedor;
    private final EliminarProveedor eliminarProveedor;

    public ProveedorController(ListarProveedores listarProveedores,
                               GuardarProveedor guardarProveedor,
                               EliminarProveedor eliminarProveedor) {
        this.listarProveedores = listarProveedores;
        this.guardarProveedor = guardarProveedor;
        this.eliminarProveedor = eliminarProveedor;
    }

    @GetMapping
    public List<ProveedorResponse> listar(UsuarioSesion actor) {
        return listarProveedores.ejecutar(actor).stream().map(ProveedorResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarProveedorRequest body) {
        return ProveedorResponse.de(guardarProveedor.ejecutar(
                actor, null, body.razonSocial(), body.ruc(), body.telefono(), body.direccion()));
    }

    @PutMapping("/{id}")
    public ProveedorResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                        @Valid @RequestBody GuardarProveedorRequest body) {
        return ProveedorResponse.de(guardarProveedor.ejecutar(
                actor, id, body.razonSocial(), body.ruc(), body.telefono(), body.direccion()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarProveedor.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
