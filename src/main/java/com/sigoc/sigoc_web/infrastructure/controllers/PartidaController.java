package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarPartidaUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PartidaDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-04: Gestión de partidas presupuestales y materiales presupuestados por partida.
 */
@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final GestionarPartidaUseCase gestionarPartida;

    public PartidaController(GestionarPartidaUseCase gestionarPartida) {
        this.gestionarPartida = gestionarPartida;
    }

    @GetMapping
    public List<PartidaResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return gestionarPartida.listar(actor, idObra).stream().map(PartidaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidaResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarPartidaRequest body) {
        return PartidaResponse.de(gestionarPartida.guardar(
                actor, null, body.idObra(), body.nombre(), body.unidadMedida(),
                body.metradoPlanificado(), body.pesoPresupuestal()));
    }

    @PutMapping("/{id}")
    public PartidaResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                      @Valid @RequestBody GuardarPartidaRequest body) {
        return PartidaResponse.de(gestionarPartida.guardar(
                actor, id, body.idObra(), body.nombre(), body.unidadMedida(),
                body.metradoPlanificado(), body.pesoPresupuestal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarPartida.eliminar(actor, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/materiales")
    public List<PartidaMaterialResponse> listarMateriales(UsuarioSesion actor, @PathVariable Integer id) {
        return gestionarPartida.listarMateriales(actor, id).stream().map(PartidaMaterialResponse::de).toList();
    }

    @PostMapping("/{id}/materiales")
    public PartidaMaterialResponse asignarMaterial(UsuarioSesion actor, @PathVariable Integer id,
                                                   @Valid @RequestBody AsignarMaterialRequest body) {
        return PartidaMaterialResponse.de(gestionarPartida.asignarMaterial(
                actor, id, body.idMaterial(), body.cantidadPresupuestada()));
    }

    @DeleteMapping("/materiales/{idPartidaMaterial}")
    public ResponseEntity<Void> eliminarMaterial(UsuarioSesion actor, @PathVariable Integer idPartidaMaterial) {
        gestionarPartida.eliminarMaterial(actor, idPartidaMaterial);
        return ResponseEntity.noContent().build();
    }
}
