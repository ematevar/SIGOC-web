package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GestionarValorizacionPagoUseCase {
    Valorizacion registrarValorizacion(UsuarioSesion actor, Integer idObra,
                                       LocalDate periodoInicio, LocalDate periodoFin, LocalDate fechaEmision);

    Comprobante emitirComprobante(UsuarioSesion actor, Integer idValorizacion, String tipoComprobante, String serieNumero);

    Pago procesarPago(UsuarioSesion actor, Integer idObra, Integer idValorizacion, Integer idComprobante,
                      BigDecimal monto, String metodoPago, String codigoOperacion);
}
