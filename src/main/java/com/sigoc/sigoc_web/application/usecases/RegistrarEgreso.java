package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.EgresoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * RF-15: registra un egreso directo de tipo 'Otro' asociado a una obra activa.
 */
public class RegistrarEgreso {
    private final EgresoPort egresoPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public RegistrarEgreso(EgresoPort egresoPort, ObraPort obraPort, Clock clock) {
        this.egresoPort = egresoPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    public Egreso ejecutar(UsuarioSesion actor, Integer idObra, LocalDate fecha,
                          String concepto, BigDecimal monto) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.CONTADOR);
        BigDecimal montoBd = monto == null ? null : monto.setScale(2, RoundingMode.HALF_UP);
        if (idObra == null || idObra <= 0 || fecha == null || fecha.isAfter(LocalDate.now(clock))
                || concepto == null || concepto.isBlank()
                || montoBd == null || montoBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Verifica obra, fecha, concepto y monto válido.");
        }

        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra no existe o está cerrada (RF-15).");
        }

        String conceptoLimpio = concepto.trim();
        if (conceptoLimpio.length() > 150) {
            conceptoLimpio = conceptoLimpio.substring(0, 150);
        }

        Egreso egreso = new Egreso();
        egreso.setIdObra(idObra);
        egreso.setFecha(fecha);
        egreso.setTipoEgreso("Otro");
        egreso.setConcepto(conceptoLimpio);
        egreso.setMonto(montoBd);
        egreso.setActivo(true);
        return egresoPort.guardar(egreso);
    }
}
