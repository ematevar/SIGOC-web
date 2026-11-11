package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.TipoCambioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Consulta y actualización manual del tipo de cambio referencial USD -> PEN (CONTADOR / ADMIN).
 */
public class GestionarTipoCambio {

    public static final BigDecimal TIPO_CAMBIO_DEFECTO = new BigDecimal("3.7500");

    private final TipoCambioPort tipoCambioPort;
    private final Clock clock;

    public GestionarTipoCambio(TipoCambioPort tipoCambioPort, Clock clock) {
        this.tipoCambioPort = tipoCambioPort;
        this.clock = clock;
    }

    public TipoCambio obtenerActual() {
        return tipoCambioPort.findByPar("USD", "PEN")
                .orElseGet(() -> new TipoCambio(null, "USD", "PEN", TIPO_CAMBIO_DEFECTO, LocalDateTime.now(clock), null));
    }

    public TipoCambio actualizar(UsuarioSesion actor, BigDecimal valor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        BigDecimal escala = valor == null ? null : valor.setScale(4, RoundingMode.HALF_UP);
        if (escala == null || escala.compareTo(BigDecimal.ZERO) <= 0 || escala.compareTo(new BigDecimal("20")) > 0) {
            throw new ReglaNegocioException("Ingresa un tipo de cambio USD/PEN válido (mayor que 0 y hasta 20.0000).");
        }
        TipoCambio tc = new TipoCambio(null, "USD", "PEN", escala, LocalDateTime.now(clock),
                actor != null ? actor.idUsuario() : null);
        return tipoCambioPort.guardar(tc);
    }
}
