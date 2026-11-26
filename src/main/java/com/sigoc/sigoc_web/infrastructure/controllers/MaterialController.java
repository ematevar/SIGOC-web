package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarMaterialUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.MaterialDtos.GuardarMaterialRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.MaterialDtos.MaterialResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-06: Catálogo de materiales de construcción y alertas de stock mínimo.
 */
@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final GestionarMaterialUseCase gestionarMaterial;

    public MaterialController(GestionarMaterialUseCase gestionarMaterial) {
        this.gestionarMaterial = gestionarMaterial;
    }

    @GetMapping
    public List<MaterialResponse> listar(UsuarioSesion actor) {
        return gestionarMaterial.listar(actor).stream().map(MaterialResponse::de).toList();
    }

    @GetMapping("/alertas")
    public List<MaterialResponse> alertas(UsuarioSesion actor) {
        return gestionarMaterial.listarAlertasStock(actor).stream().map(MaterialResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarMaterialRequest body) {
        return MaterialResponse.de(gestionarMaterial.guardar(
                actor, null, body.nombre(), body.unidadMedida(), body.stockMinimo(), body.costoUnitarioRef()));
    }

    @PutMapping("/{id}")
    public MaterialResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                       @Valid @RequestBody GuardarMaterialRequest body) {
        return MaterialResponse.de(gestionarMaterial.guardar(
                actor, id, body.nombre(), body.unidadMedida(), body.stockMinimo(), body.costoUnitarioRef()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarMaterial.eliminar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
