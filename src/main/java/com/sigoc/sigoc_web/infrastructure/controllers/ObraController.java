package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.CerrarObra;
import com.sigoc.sigoc_web.application.usecases.EliminarObra;
import com.sigoc.sigoc_web.application.usecases.GuardarObra;
import com.sigoc.sigoc_web.application.usecases.ListarObras;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ObraDtos.GuardarObraRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ObraDtos.ObraResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-03: gestión de obras civiles (consulta general autenticada, escritura solo Administrador).
 */
@RestController
@RequestMapping("/api/obras")
public class ObraController {

    private final ListarObras listarObras;
    private final GuardarObra guardarObra;
    private final EliminarObra eliminarObra;
    private final CerrarObra cerrarObra;

    public ObraController(ListarObras listarObras, GuardarObra guardarObra,
                          EliminarObra eliminarObra, CerrarObra cerrarObra) {
        this.listarObras = listarObras;
        this.guardarObra = guardarObra;
        this.eliminarObra = eliminarObra;
        this.cerrarObra = cerrarObra;
    }

    @GetMapping
    public List<ObraResponse> listar(UsuarioSesion actor) {
        return listarObras.ejecutar(actor).stream().map(ObraResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObraResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarObraRequest body) {
        return ObraResponse.de(guardarObra.ejecutar(
                actor, null, body.nombre(), body.cliente(), body.ubicacion(),
                body.presupuestoTotal(), body.fechaInicioPlan(), body.fechaFinPlan(), body.estado()));
    }

    @PutMapping("/{id}")
    public ObraResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                   @Valid @RequestBody GuardarObraRequest body) {
        return ObraResponse.de(guardarObra.ejecutar(
                actor, id, body.nombre(), body.cliente(), body.ubicacion(),
                body.presupuestoTotal(), body.fechaInicioPlan(), body.fechaFinPlan(), body.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarObra.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(UsuarioSesion actor, @PathVariable Integer id) {
        cerrarObra.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
