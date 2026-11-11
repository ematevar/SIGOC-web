package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * RF-03: crea o actualiza una obra civil validando presupuesto positivo y coherencia de fechas.
 */
public class GuardarObra {

    private final ObraPort obraPort;

    public GuardarObra(ObraPort obraPort) {
        this.obraPort = obraPort;
    }

    public Obra ejecutar(UsuarioSesion actor, Integer idObra, String nombre, String cliente,
                         String ubicacion, BigDecimal presupuestoTotal,
                         LocalDate fechaInicioPlan, LocalDate fechaFinPlan, String estado) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: nombre.");
        }
        if (cliente == null || cliente.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: cliente.");
        }
        if (presupuestoTotal == null || presupuestoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El campo presupuesto_total debe ser mayor que cero.");
        }
        if (fechaInicioPlan == null || fechaFinPlan == null) {
            throw new ReglaNegocioException("Las fechas planificadas de inicio y fin son obligatorias.");
        }
        if (fechaFinPlan.isBefore(fechaInicioPlan)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Obra obra;
        if (idObra != null) {
            obra = obraPort.findById(idObra)
                    .filter(Obra::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        } else {
            obra = new Obra();
            obra.setActivo(true);
        }

        obra.setNombre(nombre.trim());
        obra.setCliente(cliente.trim());
        obra.setUbicacion(ubicacion == null || ubicacion.isBlank() ? null : ubicacion.trim());
        obra.setPresupuestoTotal(presupuestoTotal.setScale(2, RoundingMode.HALF_UP));
        obra.setFechaInicioPlan(fechaInicioPlan);
        obra.setFechaFinPlan(fechaFinPlan);
        if (estado != null && !estado.isBlank()) {
            obra.setEstado(estado.trim());
        } else if (obra.getEstado() == null || obra.getEstado().isBlank()) {
            obra.setEstado("Planificada");
        }
        return obraPort.guardar(obra);
    }
}
