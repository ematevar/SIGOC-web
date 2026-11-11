package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.GestionarTipoCambio;
import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tipo-cambio")
public class TipoCambioController {

    private final GestionarTipoCambio gestionarTipoCambio;

    public TipoCambioController(GestionarTipoCambio gestionarTipoCambio) {
        this.gestionarTipoCambio = gestionarTipoCambio;
    }

    @GetMapping
    public TipoCambioResponse obtener(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, com.sigoc.sigoc_web.domain.models.RolSistema.values());
        return TipoCambioResponse.de(gestionarTipoCambio.obtenerActual());
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
