package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.EliminarMaterial;
import com.sigoc.sigoc_web.aplication.usecases.GuardarMaterial;
import com.sigoc.sigoc_web.aplication.usecases.ListarAlertasStock;
import com.sigoc.sigoc_web.aplication.usecases.ListarMateriales;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.MaterialDtos.GuardarMaterialRequest;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.MaterialDtos.MaterialResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-06: catálogo de materiales de construcción y alertas de stock mínimo.
 */
@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final ListarMateriales listarMateriales;
    private final ListarAlertasStock listarAlertasStock;
    private final GuardarMaterial guardarMaterial;
    private final EliminarMaterial eliminarMaterial;

    public MaterialController(ListarMateriales listarMateriales,
                              ListarAlertasStock listarAlertasStock,
                              GuardarMaterial guardarMaterial,
                              EliminarMaterial eliminarMaterial) {
        this.listarMateriales = listarMateriales;
        this.listarAlertasStock = listarAlertasStock;
        this.guardarMaterial = guardarMaterial;
        this.eliminarMaterial = eliminarMaterial;
    }

    @GetMapping
    public List<MaterialResponse> listar(UsuarioSesion actor) {
        return listarMateriales.ejecutar(actor).stream().map(MaterialResponse::de).toList();
    }

    @GetMapping("/alertas")
    public List<MaterialResponse> alertas(UsuarioSesion actor) {
        return listarAlertasStock.ejecutar(actor).stream().map(MaterialResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarMaterialRequest body) {
        return MaterialResponse.de(guardarMaterial.ejecutar(
                actor, null, body.nombre(), body.unidadMedida(), body.stockMinimo(), body.costoUnitarioRef()));
    }

    @PutMapping("/{id}")
    public MaterialResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                       @Valid @RequestBody GuardarMaterialRequest body) {
        return MaterialResponse.de(guardarMaterial.ejecutar(
                actor, id, body.nombre(), body.unidadMedida(), body.stockMinimo(), body.costoUnitarioRef()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarMaterial.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
