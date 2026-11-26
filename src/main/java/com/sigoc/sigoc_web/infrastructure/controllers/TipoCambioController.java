package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarTipoCambioUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CU-21: Consulta y actualización del tipo de cambio USD/PEN.
 */
@RestController
@RequestMapping("/api/tipo-cambio")
public class TipoCambioController {

    private final GestionarTipoCambioUseCase gestionarTipoCambio;

    public TipoCambioController(GestionarTipoCambioUseCase gestionarTipoCambio) {
        this.gestionarTipoCambio = gestionarTipoCambio;
    }

    @GetMapping
    public TipoCambioResponse obtener(UsuarioSesion actor) {
        return TipoCambioResponse.de(gestionarTipoCambio.obtenerActual(actor));
    }

    @PutMapping
    public TipoCambioResponse actualizar(UsuarioSesion actor, @Valid @RequestBody ActualizarTipoCambioRequest body) {
        return TipoCambioResponse.de(gestionarTipoCambio.actualizar(actor, body.valor()));
    }

    public record ActualizarTipoCambioRequest(@NotNull @DecimalMin("0.0001") BigDecimal valor) {
    }

    public record TipoCambioResponse(
            String monedaOrigen,
            String monedaDestino,
            BigDecimal valor,
            LocalDateTime fechaActualizacion
    ) {
        public static TipoCambioResponse de(TipoCambio tc) {
            return new TipoCambioResponse(tc.getMonedaOrigen(), tc.getMonedaDestino(),
                    tc.getValor(), tc.getFechaActualizacion());
        }
    }
}
