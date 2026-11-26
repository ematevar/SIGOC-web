package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;

/**
 * CU-21: Consulta y actualización del tipo de cambio USD/PEN.
 */
public interface GestionarTipoCambioUseCase {
    TipoCambio obtenerActual(UsuarioSesion actor);

    TipoCambio actualizar(UsuarioSesion actor, BigDecimal nuevoValor);
}
