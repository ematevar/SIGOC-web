package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarAvances;
import com.sigoc.sigoc_web.application.usecases.ObtenerAvanceObra;
import com.sigoc.sigoc_web.application.usecases.RegistrarAvance;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.AvanceDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-05: avances físicos por partida y cálculo de avance acumulado ponderado de obra.
 */
@RestController
@RequestMapping("/api/avances")
public class AvanceController {

    private final ListarAvances listarAvances;
    private final RegistrarAvance registrarAvance;
    private final ObtenerAvanceObra obtenerAvanceObra;

    public AvanceController(ListarAvances listarAvances,
                            RegistrarAvance registrarAvance,
                            ObtenerAvanceObra obtenerAvanceObra) {
        this.listarAvances = listarAvances;
        this.registrarAvance = registrarAvance;
        this.obtenerAvanceObra = obtenerAvanceObra;
    }

    @GetMapping
    public List<AvanceResponse> listar(UsuarioSesion actor,
                                       @RequestParam(required = false) Integer idPartida,
                                       @RequestParam(required = false) Integer idObra) {
        return listarAvances.ejecutar(actor, idPartida, idObra).stream().map(AvanceResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RegistroAvanceResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarAvanceRequest body) {
        var resultado = registrarAvance.ejecutar(
                actor, body.idPartida(), body.fechaReporte(), body.porcentajeAvance(), body.observaciones());
        return new RegistroAvanceResponse(AvanceResponse.de(resultado.avance()), resultado.avanceObra());
    }

    @GetMapping("/obra/{idObra}")
    public AvanceObraResponse avanceObra(UsuarioSesion actor, @PathVariable Integer idObra) {
        return new AvanceObraResponse(idObra, obtenerAvanceObra.ejecutar(actor, idObra));
    }
}
