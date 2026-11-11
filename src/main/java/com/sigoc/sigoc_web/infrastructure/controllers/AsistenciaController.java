package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarAsistencias;
import com.sigoc.sigoc_web.application.usecases.RegistrarAsistencia;
import com.sigoc.sigoc_web.application.usecases.RegistrarAsistenciaLote;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.AsistenciaResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarAsistenciaLoteRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-11: registro individual o en lote y consulta de asistencia diaria en obra.
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final ListarAsistencias listarAsistencias;
    private final RegistrarAsistencia registrarAsistencia;
    private final RegistrarAsistenciaLote registrarAsistenciaLote;

    public AsistenciaController(ListarAsistencias listarAsistencias,
                                RegistrarAsistencia registrarAsistencia,
                                RegistrarAsistenciaLote registrarAsistenciaLote) {
        this.listarAsistencias = listarAsistencias;
        this.registrarAsistencia = registrarAsistencia;
        this.registrarAsistenciaLote = registrarAsistenciaLote;
    }

    @GetMapping
    public List<AsistenciaResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarAsistencias.ejecutar(actor, idObra).stream().map(AsistenciaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AsistenciaResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarAsistenciaRequest body) {
        return AsistenciaResponse.de(registrarAsistencia.ejecutar(
                actor, body.idEmpleado(), body.idObra(), body.fecha(),
                body.horaEntrada(), body.horaSalida(), body.estado()));
    }

    @PostMapping("/lote")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<AsistenciaResponse> registrarLote(UsuarioSesion actor,
                                                  @Valid @RequestBody RegistrarAsistenciaLoteRequest body) {
        var filas = body.filas().stream()
                .map(f -> new RegistrarAsistenciaLote.FilaAsistencia(
                        f.idEmpleado(), f.estado(), f.horaEntrada(), f.horaSalida()))
                .toList();
        return registrarAsistenciaLote.ejecutar(actor, body.idObra(), body.fecha(), filas)
                .stream().map(AsistenciaResponse::de).toList();
    }
}
