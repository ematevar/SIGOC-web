package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarAvanceUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.AvanceDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-05: Avances físicos por partida y cálculo de avance acumulado ponderado de obra.
 */
@RestController
@RequestMapping("/api/avances")
public class AvanceController {

    private final GestionarAvanceUseCase gestionarAvance;

    public AvanceController(GestionarAvanceUseCase gestionarAvance) {
        this.gestionarAvance = gestionarAvance;
    }

    @GetMapping
    public List<AvanceResponse> listar(UsuarioSesion actor,
                                       @RequestParam(required = false) Integer idPartida,
                                       @RequestParam(required = false) Integer idObra) {
        return gestionarAvance.listar(actor, idPartida, idObra).stream().map(AvanceResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroAvanceResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarAvanceRequest body) {
        var resultado = gestionarAvance.registrar(
                actor, body.idPartida(), body.fechaReporte(), body.porcentajeAvance(), body.observaciones());
        return new RegistroAvanceResponse(AvanceResponse.de(resultado.avance()), resultado.avanceObra());
    }

    @GetMapping("/obra/{idObra}")
    public AvanceObraResponse avanceObra(UsuarioSesion actor, @PathVariable Integer idObra) {
        return new AvanceObraResponse(idObra, gestionarAvance.obtenerAvanceObra(actor, idObra));
    }
}
