package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

/**
 * RF-03: crea o actualiza una obra civil validando presupuesto positivo, coherencia de fechas,
 * vínculo opcional con cliente activo e inmutabilidad de obras finalizadas (C09).
 */
public class GuardarObra {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "Planificada", "En Ejecución", "Suspendida", "Finalizada");

    private final ObraPort obraPort;
    private final ClientePort clientePort;

    public GuardarObra(ObraPort obraPort) {
        this(obraPort, null);
    }

    public GuardarObra(ObraPort obraPort, ClientePort clientePort) {
        this.obraPort = obraPort;
        this.clientePort = clientePort;
    }

    public Obra ejecutar(UsuarioSesion actor, Integer idObra, String nombre, String cliente,
                         String ubicacion, BigDecimal presupuestoTotal,
                         LocalDate fechaInicioPlan, LocalDate fechaFinPlan, String estado) {
        return ejecutar(actor, idObra, null, nombre, cliente, ubicacion, presupuestoTotal,
                fechaInicioPlan, fechaFinPlan, estado);
    }

    public Obra ejecutar(UsuarioSesion actor, Integer idObra, Integer idCliente,
                         String nombre, String cliente, String ubicacion,
                         BigDecimal presupuestoTotal, LocalDate fechaInicioPlan,
                         LocalDate fechaFinPlan, String estado) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: nombre.");
        }

        Integer idClienteFinal = idCliente;
        String nombreClienteFinal = cliente == null ? "" : cliente.trim();
        if (idCliente != null && idCliente > 0) {
            if (clientePort != null) {
                Cliente cli = clientePort.findById(idCliente)
                        .filter(Cliente::isActivo)
                        .orElseThrow(() -> new ReglaNegocioException(
                                "El cliente seleccionado no existe o se encuentra inactivo."));
                nombreClienteFinal = cli.getNombreRazonSocial();
            }
        } else {
            idClienteFinal = null;
        }

        if (nombreClienteFinal.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: cliente.");
        }

        BigDecimal presupuestoBd = presupuestoTotal == null
                ? null
                : presupuestoTotal.setScale(2, RoundingMode.HALF_UP);
        if (presupuestoBd == null || presupuestoBd.compareTo(BigDecimal.ZERO) <= 0) {
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
            obra = obraPort.findByIdParaActualizar(idObra)
                    .filter(Obra::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
            if (!obra.isOperable()) {
                throw new ReglaNegocioException(
                        "Una obra finalizada no puede modificarse ni reabrirse (RF-03).");
            }
        } else {
            obra = new Obra();
            obra.setActivo(true);
        }

        if (estado != null && !estado.isBlank()) {
            String estadoLimpio = estado.trim();
            if (!ESTADOS_VALIDOS.contains(estadoLimpio)) {
                throw new ReglaNegocioException("Estado de obra no válido.");
            }
            obra.setEstado(estadoLimpio);
        } else if (obra.getEstado() == null || obra.getEstado().isBlank()) {
            obra.setEstado("Planificada");
        }

        obra.setIdCliente(idClienteFinal);
        obra.setNombre(nombre.trim());
        obra.setCliente(nombreClienteFinal);
        obra.setUbicacion(ubicacion == null || ubicacion.isBlank() ? null : ubicacion.trim());
        obra.setPresupuestoTotal(presupuestoBd);
        obra.setFechaInicioPlan(fechaInicioPlan);
        obra.setFechaFinPlan(fechaFinPlan);
        return obraPort.guardar(obra);
    }
}
