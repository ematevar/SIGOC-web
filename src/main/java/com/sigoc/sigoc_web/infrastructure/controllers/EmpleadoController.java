package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarEmpleadoUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.EmpleadoResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.PersonalDtos.GuardarEmpleadoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-10: Gestión de trabajadores.
 */
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final GestionarEmpleadoUseCase gestionarEmpleado;

    public EmpleadoController(GestionarEmpleadoUseCase gestionarEmpleado) {
        this.gestionarEmpleado = gestionarEmpleado;
    }

    @GetMapping
    public List<EmpleadoResponse> listar(UsuarioSesion actor) {
        return gestionarEmpleado.listar(actor).stream().map(EmpleadoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarEmpleadoRequest body) {
        return EmpleadoResponse.de(gestionarEmpleado.guardar(
                actor, null, body.nombresApellidos(), body.dni(),
                body.cargo(), body.jornalDiario(), body.fechaIngreso()));
    }

    @PutMapping("/{id}")
    public EmpleadoResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                       @Valid @RequestBody GuardarEmpleadoRequest body) {
        return EmpleadoResponse.de(gestionarEmpleado.guardar(
                actor, id, body.nombresApellidos(), body.dni(),
                body.cargo(), body.jornalDiario(), body.fechaIngreso()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarEmpleado.eliminar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
