package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarConsumos;
import com.sigoc.sigoc_web.application.usecases.ObtenerConsumoAcumuladoPartida;
import com.sigoc.sigoc_web.application.usecases.RegistrarConsumo;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.ConsumoDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-08: consumos de materiales por partida y verificación de alerta de stock mínimo.
 */
@RestController
@RequestMapping("/api/consumos")
public class ConsumoController {

    private final ListarConsumos listarConsumos;
    private final RegistrarConsumo registrarConsumo;
    private final ObtenerConsumoAcumuladoPartida obtenerConsumoAcumulado;

    public ConsumoController(ListarConsumos listarConsumos,
                             RegistrarConsumo registrarConsumo,
                             ObtenerConsumoAcumuladoPartida obtenerConsumoAcumulado) {
        this.listarConsumos = listarConsumos;
        this.registrarConsumo = registrarConsumo;
        this.obtenerConsumoAcumulado = obtenerConsumoAcumulado;
    }

    @GetMapping
    public List<ConsumoResponse> listar(UsuarioSesion actor, @RequestParam(required = false) Integer idPartida) {
        return listarConsumos.ejecutar(actor, idPartida).stream().map(ConsumoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RegistroConsumoResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarConsumoRequest body) {
        var res = registrarConsumo.ejecutar(
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
                idPartida, idMaterial, obtenerConsumoAcumulado.ejecutar(actor, idPartida, idMaterial));
    }
}
