package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Ingreso;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * RF-13: registra un ingreso financiero dentro del cronograma planificado de una obra activa.
 */
public class RegistrarIngreso {
    private final IngresoPort ingresoPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public RegistrarIngreso(IngresoPort ingresoPort, ObraPort obraPort, Clock clock) {
        this.ingresoPort = ingresoPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    public Ingreso ejecutar(UsuarioSesion actor, Integer idObra, LocalDate fecha,
                           String concepto, BigDecimal monto) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idObra == null || idObra <= 0 || fecha == null || fecha.isAfter(LocalDate.now(clock))
                || concepto == null || concepto.isBlank()
                || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Verifica obra, fecha, concepto y monto válido.");
        }

        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra no existe o está cerrada (RF-13).");
        }
        if ((obra.getFechaInicioPlan() != null && fecha.isBefore(obra.getFechaInicioPlan()))
                || (obra.getFechaFinPlan() != null && fecha.isAfter(obra.getFechaFinPlan()))) {
            throw new ReglaNegocioException(
                    "La fecha del ingreso debe estar dentro del rango planificado de la obra (RF-13).");
        }

        String conceptoLimpio = concepto.trim();
        if (conceptoLimpio.length() > 150) {
            conceptoLimpio = conceptoLimpio.substring(0, 150);
        }

        Ingreso ingreso = new Ingreso();
        ingreso.setIdObra(idObra);
        ingreso.setFecha(fecha);
        ingreso.setConcepto(conceptoLimpio);
        ingreso.setMonto(monto.setScale(2, RoundingMode.HALF_UP));
        ingreso.setActivo(true);
        return ingresoPort.guardar(ingreso);
    }
}
