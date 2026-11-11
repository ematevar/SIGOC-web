package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.ListarAsistencias;
import com.sigoc.sigoc_web.aplication.usecases.RegistrarAsistencia;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.PersonalDtos.AsistenciaResponse;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.PersonalDtos.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-11: registro y consulta de asistencia diaria en obra.
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final ListarAsistencias listarAsistencias;
    private final RegistrarAsistencia registrarAsistencia;

    public AsistenciaController(ListarAsistencias listarAsistencias, RegistrarAsistencia registrarAsistencia) {
        this.listarAsistencias = listarAsistencias;
        this.registrarAsistencia = registrarAsistencia;
    }

    @GetMapping
    public List<AsistenciaResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idObra) {
        return listarAsistencias.ejecutar(actor, idObra).stream().map(AsistenciaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarAsistenciaRequest body) {
        return AsistenciaResponse.de(registrarAsistencia.ejecutar(
                actor, body.idEmpleado(), body.idObra(), body.fecha(),
                body.horaEntrada(), body.horaSalida(), body.estado()));
    }
}
