package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarTipoCambioUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.TipoCambioPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * CU-21: Consulta y actualización manual del tipo de cambio referencial USD -> PEN (CONTADOR).
 */
public class GestionarTipoCambio implements GestionarTipoCambioUseCase {

    public static final BigDecimal TIPO_CAMBIO_DEFECTO = new BigDecimal("3.7500");

    private final TipoCambioPort tipoCambioPort;
    private final Clock clock;
    private final AuditoriaEventoPort auditoriaPort;

    public GestionarTipoCambio(TipoCambioPort tipoCambioPort, Clock clock) {
        this(tipoCambioPort, clock, null);
    }

    public GestionarTipoCambio(TipoCambioPort tipoCambioPort, Clock clock, AuditoriaEventoPort auditoriaPort) {
        this.tipoCambioPort = tipoCambioPort;
        this.clock = clock;
        this.auditoriaPort = auditoriaPort;
    }

    public TipoCambio obtenerActual() {
        return tipoCambioPort.findByPar("USD", "PEN")
                .orElseGet(() -> new TipoCambio(null, "USD", "PEN", TIPO_CAMBIO_DEFECTO, LocalDateTime.now(clock), null));
    }

    @Override
    public TipoCambio obtenerActual(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.values());
        return obtenerActual();
    }

    @Override
    public TipoCambio actualizar(UsuarioSesion actor, BigDecimal valor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_TIPO_CAMBIO);
        BigDecimal escala = valor == null ? null : valor.setScale(4, RoundingMode.HALF_UP);
        if (escala == null || escala.compareTo(BigDecimal.ZERO) <= 0 || escala.compareTo(new BigDecimal("20")) > 0) {
            throw new ReglaNegocioException("Ingresa un tipo de cambio USD/PEN válido (mayor que 0 y hasta 20.0000).");
        }
        TipoCambio tc = new TipoCambio(null, "USD", "PEN", escala, LocalDateTime.now(clock),
                actor != null ? actor.idUsuario() : null);
        TipoCambio guardado = tipoCambioPort.guardar(tc);
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, "CAMBIO_TIPO_CAMBIO", "tipo_cambio", "USD/PEN",
                    "EXITO", "Nuevo tipo de cambio USD/PEN: " + escala));
        }
        return guardado;
    }
}
