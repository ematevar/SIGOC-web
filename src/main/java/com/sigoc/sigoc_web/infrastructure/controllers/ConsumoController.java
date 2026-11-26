package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarConsumoUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ConsumoDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-08: Consumos de materiales por partida y verificación de alerta de stock mínimo.
 */
@RestController
@RequestMapping("/api/consumos")
public class ConsumoController {

    private final GestionarConsumoUseCase gestionarConsumo;

    public ConsumoController(GestionarConsumoUseCase gestionarConsumo) {
        this.gestionarConsumo = gestionarConsumo;
    }

    @GetMapping
    public List<ConsumoResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idPartida) {
        return gestionarConsumo.listar(actor, idPartida).stream().map(ConsumoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroConsumoResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarConsumoRequest body) {
        var res = gestionarConsumo.registrar(
                actor, body.idMaterial(), body.idPartida(), body.fechaConsumo(), body.cantidad());
        return new RegistroConsumoResponse(
                ConsumoResponse.de(res.consumo()),
                res.stockResultante(),
                res.alertaStockMinimo()
        );
    }

    @GetMapping("/acumulado")
    public ConsumoAcumuladoResponse acumulado(UsuarioSesion actor,
                                              @RequestParam Integer idPartida,
                                              @RequestParam Integer idMaterial) {
        return new ConsumoAcumuladoResponse(
                idPartida, idMaterial, gestionarConsumo.obtenerAcumulado(actor, idPartida, idMaterial));
    }
}
