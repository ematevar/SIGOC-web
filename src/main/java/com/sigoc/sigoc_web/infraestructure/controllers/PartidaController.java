package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.*;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.PartidaDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-04 y RF-16b: gestión de partidas presupuestales y materiales presupuestados por partida.
 */
@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final ListarPartidas listarPartidas;
    private final GuardarPartida guardarPartida;
    private final EliminarPartida eliminarPartida;
    private final ListarMaterialesPartida listarMaterialesPartida;
    private final AsignarMaterialPartida asignarMaterialPartida;
    private final EliminarMaterialPartida eliminarMaterialPartida;

    public PartidaController(ListarPartidas listarPartidas,
                             GuardarPartida guardarPartida,
                             EliminarPartida eliminarPartida,
                             ListarMaterialesPartida listarMaterialesPartida,
                             AsignarMaterialPartida asignarMaterialPartida,
                             EliminarMaterialPartida eliminarMaterialPartida) {
        this.listarPartidas = listarPartidas;
        this.guardarPartida = guardarPartida;
        this.eliminarPartida = eliminarPartida;
        this.listarMaterialesPartida = listarMaterialesPartida;
        this.asignarMaterialPartida = asignarMaterialPartida;
        this.eliminarMaterialPartida = eliminarMaterialPartida;
    }

    @GetMapping
    public List<PartidaResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarPartidas.ejecutar(actor, idObra).stream().map(PartidaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidaResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarPartidaRequest body) {
        return PartidaResponse.de(guardarPartida.ejecutar(
                actor, null, body.idObra(), body.nombre(), body.unidadMedida(),
                body.metradoPlanificado(), body.pesoPresupuestal()));
    }

    @PutMapping("/{id}")
    public PartidaResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                      @Valid @RequestBody GuardarPartidaRequest body) {
        return PartidaResponse.de(guardarPartida.ejecutar(
                actor, id, body.idObra(), body.nombre(), body.unidadMedida(),
                body.metradoPlanificado(), body.pesoPresupuestal()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarPartida.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/materiales")
    public List<PartidaMaterialResponse> listarMateriales(UsuarioSesion actor, @PathVariable Integer id) {
        return listarMaterialesPartida.ejecutar(actor, id).stream().map(PartidaMaterialResponse::de).toList();
    }

    @PostMapping("/{id}/materiales")
    public PartidaMaterialResponse asignarMaterial(UsuarioSesion actor, @PathVariable Integer id,
                                                   @Valid @RequestBody AsignarMaterialRequest body) {
        return PartidaMaterialResponse.de(asignarMaterialPartida.ejecutar(
                actor, id, body.idMaterial(), body.cantidadPresupuestada()));
    }

    @DeleteMapping("/materiales/{idPartidaMaterial}")
    public ResponseEntity<Void> eliminarMaterial(UsuarioSesion actor, @PathVariable Integer idPartidaMaterial) {
        eliminarMaterialPartida.ejecutar(actor, idPartidaMaterial);
        return ResponseEntity.noContent().build();
    }
}
