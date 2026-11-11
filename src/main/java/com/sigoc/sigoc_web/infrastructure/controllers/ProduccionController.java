package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarProduccion;
import com.sigoc.sigoc_web.application.usecases.RegistrarProduccion;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.ProduccionResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.RegistrarProduccionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-12: registro y consulta de producción diaria por trabajador y partida.
 */
@RestController
@RequestMapping("/api/produccion")
public class ProduccionController {

    private final ListarProduccion listarProduccion;
    private final RegistrarProduccion registrarProduccion;

    public ProduccionController(ListarProduccion listarProduccion, RegistrarProduccion registrarProduccion) {
        this.listarProduccion = listarProduccion;
        this.registrarProduccion = registrarProduccion;
    }

    @GetMapping
    public List<ProduccionResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idPartida) {
        return listarProduccion.ejecutar(actor, idPartida).stream().map(ProduccionResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduccionResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarProduccionRequest body) {
        return ProduccionResponse.de(registrarProduccion.ejecutar(
                actor, body.idEmpleado(), body.idPartida(), body.fecha(), body.cantidadProducida()));
    }
}
