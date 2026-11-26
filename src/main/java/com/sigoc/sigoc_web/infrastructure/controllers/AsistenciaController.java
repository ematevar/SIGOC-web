package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarAsistenciaUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.AsistenciaResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarAsistenciaLoteRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-11: Registro individual o en lote y consulta de asistencia diaria en obra.
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final GestionarAsistenciaUseCase gestionarAsistencia;

    public AsistenciaController(GestionarAsistenciaUseCase gestionarAsistencia) {
        this.gestionarAsistencia = gestionarAsistencia;
    }

    @GetMapping
    public List<AsistenciaResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return gestionarAsistencia.listar(actor, idObra).stream().map(AsistenciaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarAsistenciaRequest body) {
        return AsistenciaResponse.de(gestionarAsistencia.registrar(
                actor, body.idEmpleado(), body.idObra(), body.fecha(),
                body.horaEntrada(), body.horaSalida(), body.estado()));
    }

    @PostMapping("/lote")
    @ResponseStatus(HttpStatus.CREATED)
    public List<AsistenciaResponse> registrarLote(UsuarioSesion actor,
                                                  @Valid @RequestBody RegistrarAsistenciaLoteRequest body) {
        var filas = body.filas().stream()
                .map(f -> new GestionarAsistenciaUseCase.FilaAsistencia(
                        f.idEmpleado(), f.estado(), f.horaEntrada(), f.horaSalida()))
                .toList();
        return gestionarAsistencia.registrarLote(actor, body.idObra(), body.fecha(), filas)
                .stream().map(AsistenciaResponse::de).toList();
    }
}
