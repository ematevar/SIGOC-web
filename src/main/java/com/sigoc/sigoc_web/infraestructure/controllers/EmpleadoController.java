package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.aplication.usecases.EliminarEmpleado;
import com.sigoc.sigoc_web.aplication.usecases.GuardarEmpleado;
import com.sigoc.sigoc_web.aplication.usecases.ListarEmpleados;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.PersonalDtos.EmpleadoResponse;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.PersonalDtos.GuardarEmpleadoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-10: gestión de trabajadores (Administrador y Residente de Obra).
 */
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final ListarEmpleados listarEmpleados;
    private final GuardarEmpleado guardarEmpleado;
    private final EliminarEmpleado eliminarEmpleado;

    public EmpleadoController(ListarEmpleados listarEmpleados,
                              GuardarEmpleado guardarEmpleado,
                              EliminarEmpleado eliminarEmpleado) {
        this.listarEmpleados = listarEmpleados;
        this.guardarEmpleado = guardarEmpleado;
        this.eliminarEmpleado = eliminarEmpleado;
    }

    @GetMapping
    public List<EmpleadoResponse> listar(UsuarioSesion actor) {
        return listarEmpleados.ejecutar(actor).stream().map(EmpleadoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarEmpleadoRequest body) {
        return EmpleadoResponse.de(guardarEmpleado.ejecutar(
                actor, null, body.nombresApellidos(), body.dni(),
                body.cargo(), body.jornalDiario(), body.fechaIngreso()));
    }

    @PutMapping("/{id}")
    public EmpleadoResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                       @Valid @RequestBody GuardarEmpleadoRequest body) {
        return EmpleadoResponse.de(guardarEmpleado.ejecutar(
                actor, id, body.nombresApellidos(), body.dni(),
                body.cargo(), body.jornalDiario(), body.fechaIngreso()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarEmpleado.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
