package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-18: Previsualización, registro y consulta de valorizaciones de obra.
 */
public interface GestionarValorizacionUseCase {

    record ResumenCalculoValorizacion(
            Integer idObra,
            String nombreObra,
            int numeroSiguiente,
            LocalDate periodoInicio,
            LocalDate periodoFin,
            BigDecimal avanceAcumulado,
            BigDecimal avanceYaValorizado,
            BigDecimal porcentajeAvancePeriodo,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total
    ) {
    }

    List<Valorizacion> listar(UsuarioSesion actor, Integer idObra);

    ResumenCalculoValorizacion previsualizar(UsuarioSesion actor, Integer idObra,
                                             LocalDate periodoInicio, LocalDate periodoFin);

    Valorizacion registrar(UsuarioSesion actor, Integer idObra,
                           LocalDate periodoInicio, LocalDate periodoFin, LocalDate fechaEmision);
}
