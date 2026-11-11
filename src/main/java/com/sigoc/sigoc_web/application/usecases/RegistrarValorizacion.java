package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RF-18 / RN-20..RN-22: Calcula automáticamente e inmutabiliza valorizaciones por rango de fechas,
 * avance físico acumulado ponderado y presupuesto contractual (subtotal + IGV 18%).
 */
public class RegistrarValorizacion {

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    private final ValorizacionPort valorizacionPort;
    private final ObraPort obraPort;
    private final PartidaPort partidaPort;
    private final AvancePartidaPort avancePartidaPort;
    private final Clock clock;

    public RegistrarValorizacion(ValorizacionPort valorizacionPort, ObraPort obraPort, Clock clock) {
        this(valorizacionPort, obraPort, null, null, clock);
    }

    public RegistrarValorizacion(ValorizacionPort valorizacionPort, ObraPort obraPort,
                                 PartidaPort partidaPort, AvancePartidaPort avancePartidaPort, Clock clock) {
        this.valorizacionPort = valorizacionPort;
        this.obraPort = obraPort;
        this.partidaPort = partidaPort;
        this.avancePartidaPort = avancePartidaPort;
        this.clock = clock;
    }

    public ResumenCalculoValorizacion previsualizar(UsuarioSesion actor, Integer idObra,
                                                      LocalDate periodoInicio, LocalDate periodoFin) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        Obra obra = validarObraYPeriodo(idObra, periodoInicio, periodoFin, false);
        return calcularResumen(obra, periodoInicio, periodoFin);
    }

    public Valorizacion ejecutar(UsuarioSesion actor, Integer idObra,
                                 LocalDate periodoInicio, LocalDate periodoFin, LocalDate fechaEmision) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        Obra obra = validarObraYPeriodo(idObra, periodoInicio, periodoFin, true);
        ResumenCalculoValorizacion calc = calcularResumen(obra, periodoInicio, periodoFin);

        if (calc.porcentajeAvancePeriodo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(
                    "No hay avance físico nuevo pendiente por valorizar hasta el " + periodoFin
                            + " (acumulado: " + calc.avanceAcumulado() + "%, ya valorizado: " + calc.avanceYaValorizado() + "%).");
        }
        if (calc.avanceAcumulado().compareTo(CIEN) > 0) {
            throw new ReglaNegocioException("El avance acumulado supera el 100.00% contractual.");
        }

        BigDecimal subtotalPrevio = valorizacionPort.findActivasByObra(idObra).stream()
                .map(v -> v.getSubtotal() != null && v.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                        ? v.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotalPrevio.add(calc.subtotal()).compareTo(obra.getPresupuestoTotal().add(new BigDecimal("0.05"))) > 0) {
            throw new ReglaNegocioException(
                    "El acumulado valorizado (sin IGV) excedería el presupuesto contractual de la obra (RN-22).");
        }

        LocalDate fecha = fechaEmision != null ? fechaEmision : LocalDate.now(clock);
        if (fecha.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de emisión no puede ser futura.");
        }

        Valorizacion val = new Valorizacion();
        val.setIdObra(idObra);
        val.setNumero(calc.numeroSiguiente());
        val.setPeriodo(periodoInicio + " a " + periodoFin);
        val.setPeriodoInicio(periodoInicio);
        val.setPeriodoFin(periodoFin);
        val.setFechaEmision(fecha);
        val.setAvanceAcumulado(calc.avanceAcumulado());
        val.setPorcentajeAvancePeriodo(calc.porcentajeAvancePeriodo());
        val.setSubtotal(calc.subtotal());
        val.setIgv(calc.igv());
        val.setTotal(calc.total());
        val.setMontoValorizado(calc.total());
        val.setEstado("PENDIENTE");
        val.setActivo(true);
        return valorizacionPort.guardar(val);
    }

    private Obra validarObraYPeriodo(Integer idObra, LocalDate inicio, LocalDate fin, boolean bloquearObra) {
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida.");
        }
        Obra obra = (bloquearObra ? obraPort.findByIdParaActualizar(idObra) : obraPort.findById(idObra)).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o se encuentra cerrada.");
        }
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Indica las fechas de inicio y fin del periodo a valorizar.");
        }
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha de fin del periodo no puede ser anterior al inicio.");
        }
        if (fin.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de fin del periodo no puede ser futura.");
        }
        List<Valorizacion> existentes = valorizacionPort.findActivasByObra(idObra);
        for (Valorizacion ex : existentes) {
            if (ex.getPeriodoInicio() != null && ex.getPeriodoFin() != null) {
                boolean separado = fin.isBefore(ex.getPeriodoInicio()) || inicio.isAfter(ex.getPeriodoFin());
                if (!separado) {
                    throw new ReglaNegocioException(
                            "El periodo se solapa con la valorización N° " + ex.getNumero()
                                    + " (" + ex.getPeriodoInicio() + " a " + ex.getPeriodoFin() + ").");
                }
            }
        }
        return obra;
    }

    private ResumenCalculoValorizacion calcularResumen(Obra obra, LocalDate inicio, LocalDate fin) {
        if (partidaPort == null || avancePartidaPort == null) {
            throw new ReglaNegocioException("Faltan los puertos de partidas/avances para el cálculo automático.");
        }
        List<Partida> partidas = partidaPort.findActivasByObra(obra.getIdObra());
        if (partidas.isEmpty()) {
            throw new ReglaNegocioException("La obra no tiene partidas registradas para valorizar.");
        }
        List<AvancePartida> avancesHastaFecha = avancePartidaPort.findActivosByObra(obra.getIdObra()).stream()
                .filter(a -> !a.getFechaReporte().isAfter(fin))
                .toList();

        Map<Integer, AvancePartida> ultimoPorPartida = avancesHastaFecha.stream()
                .collect(Collectors.toMap(
                        AvancePartida::getIdPartida,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(AvancePartida::getFechaReporte)
                                .thenComparing(AvancePartida::getIdAvance, Comparator.nullsFirst(Integer::compareTo)))));

        BigDecimal avanceAcumulado = BigDecimal.ZERO;
        for (Partida p : partidas) {
            AvancePartida av = ultimoPorPartida.get(p.getIdPartida());
            if (av != null) {
                BigDecimal aporte = av.getPorcentajeAvance()
                        .multiply(p.getPesoPresupuestal())
                        .divide(CIEN, 4, RoundingMode.HALF_UP);
                avanceAcumulado = avanceAcumulado.add(aporte);
            }
        }
        avanceAcumulado = avanceAcumulado.setScale(2, RoundingMode.HALF_UP);

        List<Valorizacion> existentes = valorizacionPort.findActivasByObra(obra.getIdObra());
        BigDecimal yaValorizado = existentes.stream()
                .map(Valorizacion::getPorcentajeAvancePeriodo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal delta = avanceAcumulado.subtract(yaValorizado).setScale(2, RoundingMode.HALF_UP);
        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            delta = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal subtotal = obra.getPresupuestoTotal()
                .multiply(delta)
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(TASA_IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);
        int nextNum = existentes.stream()
                .mapToInt(v -> v.getNumero() == null ? 0 : v.getNumero())
                .max().orElse(0) + 1;

        return new ResumenCalculoValorizacion(
                obra.getIdObra(), obra.getNombre(), nextNum, inicio, fin,
                avanceAcumulado, yaValorizado, delta, subtotal, igv, total);
    }

    public record ResumenCalculoValorizacion(
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
}
