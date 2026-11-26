package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarObraUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ObraDtos.GuardarObraRequest;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ObraDtos.ObraResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-03: Gestión de obras civiles y cierre formal (escritura exclusiva de GERENTE).
 */
@RestController
@RequestMapping("/api/obras")
public class ObraController {

    private final GestionarObraUseCase gestionarObra;

    public ObraController(GestionarObraUseCase gestionarObra) {
        this.gestionarObra = gestionarObra;
    }

    @GetMapping
    public List<ObraResponse> listar(UsuarioSesion actor) {
        return gestionarObra.listar(actor).stream().map(ObraResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObraResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarObraRequest body) {
        return ObraResponse.de(gestionarObra.guardar(
                actor, null, body.idCliente(), body.nombre(), body.cliente(), body.ubicacion(),
                body.presupuestoTotal(), body.fechaInicioPlan(), body.fechaFinPlan(), body.estado()));
    }

    @PutMapping("/{id}")
    public ObraResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                   @Valid @RequestBody GuardarObraRequest body) {
        return ObraResponse.de(gestionarObra.guardar(
                actor, id, body.idCliente(), body.nombre(), body.cliente(), body.ubicacion(),
                body.presupuestoTotal(), body.fechaInicioPlan(), body.fechaFinPlan(), body.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarObra.eliminar(actor, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrar(UsuarioSesion actor, @PathVariable Integer id) {
        gestionarObra.cerrar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
