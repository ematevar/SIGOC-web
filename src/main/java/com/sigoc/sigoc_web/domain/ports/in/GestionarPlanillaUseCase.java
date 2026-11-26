package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-14: Previsualización, cierre inmutable y consulta de planillas laborales.
 */
public interface GestionarPlanillaUseCase {

    record DetalleTrabajadorPlanilla(
            Integer idEmpleado,
            String nombresApellidos,
            String dni,
            String cargo,
            long diasValidos,
            BigDecimal jornalDiario,
            BigDecimal subtotal
    ) {
    }

    record ResumenPlanilla(
            Integer idObra,
            String nombreObra,
            LocalDate desde,
            LocalDate hasta,
            BigDecimal totalPlanilla,
            List<DetalleTrabajadorPlanilla> trabajadores
    ) {
    }

    @OperacionSoloLectura
    List<CierrePlanilla> listarCierres(UsuarioSesion actor, Integer idObra);

    @OperacionSoloLectura
    ResumenPlanilla previsualizar(UsuarioSesion actor, Integer idObra, LocalDate desde, LocalDate hasta);

    CierrePlanilla cerrar(UsuarioSesion actor, Integer idObra, LocalDate periodoInicio, LocalDate periodoFin);
}
