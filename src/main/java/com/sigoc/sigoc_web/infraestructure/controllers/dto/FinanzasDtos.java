package com.sigoc.sigoc_web.infraestructure.controllers.dto;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.models.Ingreso;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FinanzasDtos {
    private FinanzasDtos() {
    }

    public record IngresoRequest(
            @NotNull Integer idObra,
            @NotNull LocalDate fecha,
            @NotBlank @Size(max = 150) String concepto,
            @NotNull @DecimalMin("0.01") BigDecimal monto
    ) {
    }

    public record IngresoResponse(
            Integer idIngreso,
            Integer idObra,
            LocalDate fecha,
            String concepto,
            BigDecimal monto
    ) {
        public static IngresoResponse de(Ingreso i) {
            return new IngresoResponse(i.getIdIngreso(), i.getIdObra(), i.getFecha(), i.getConcepto(), i.getMonto());
        }
    }

    public record EgresoRequest(
            @NotNull Integer idObra,
            @NotNull LocalDate fecha,
            @NotBlank @Size(max = 150) String concepto,
            @NotNull @DecimalMin("0.01") BigDecimal monto
    ) {
    }

    public record EgresoResponse(
            Integer idEgreso,
            Integer idObra,
            LocalDate fecha,
            String tipoEgreso,
            String concepto,
            BigDecimal monto,
            Integer idCompra,
            Integer idCierre
    ) {
        public static EgresoResponse de(Egreso e) {
            return new EgresoResponse(e.getIdEgreso(), e.getIdObra(), e.getFecha(), e.getTipoEgreso(),
                    e.getConcepto(), e.getMonto(), e.getIdCompra(), e.getIdCierre());
        }
    }

    public record CerrarPlanillaRequest(
            @NotNull Integer idObra,
            @NotNull LocalDate desde,
            @NotNull LocalDate hasta
    ) {
    }

    public record CierrePlanillaResponse(
            Integer idCierre,
            Integer idObra,
            LocalDate fechaInicioPeriodo,
            LocalDate fechaFinPeriodo,
            LocalDateTime fechaCierre,
            BigDecimal montoTotal,
            Integer idUsuario
    ) {
        public static CierrePlanillaResponse de(CierrePlanilla c) {
            return new CierrePlanillaResponse(c.getIdCierre(), c.getIdObra(), c.getFechaInicioPeriodo(),
                    c.getFechaFinPeriodo(), c.getFechaCierre(), c.getMontoTotal(), c.getIdUsuario());
        }
    }
}
