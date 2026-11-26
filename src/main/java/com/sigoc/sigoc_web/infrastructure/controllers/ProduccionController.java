package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarProduccionUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.ProduccionResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarProduccionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-12: Registro y consulta de producción diaria por trabajador y partida.
 */
@RestController
@RequestMapping("/api/produccion")
public class ProduccionController {

    private final GestionarProduccionUseCase gestionarProduccion;

    public ProduccionController(GestionarProduccionUseCase gestionarProduccion) {
        this.gestionarProduccion = gestionarProduccion;
    }

    @GetMapping
    public List<ProduccionResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idPartida) {
        return gestionarProduccion.listar(actor, idPartida).stream().map(ProduccionResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduccionResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarProduccionRequest body) {
        return ProduccionResponse.de(gestionarProduccion.registrar(
                actor, body.idEmpleado(), body.idPartida(), body.fecha(), body.cantidadProducida()));
    }
}
