package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AvancePartida;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarAvanceUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CU-05: Registro y consulta de avances físicos por partida y avance acumulado ponderado de obra.
 */
public class GestionarAvance implements GestionarAvanceUseCase {

    private static final BigDecimal CIEN_DEC = new BigDecimal("100.00");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final AvancePartidaPort avancePort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public GestionarAvance(AvancePartidaPort avancePort, PartidaPort partidaPort, ObraPort obraPort, Clock clock) {
        this.avancePort = avancePort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    @Override
    public List<AvancePartida> listar(UsuarioSesion actor, Integer idPartida, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_AVANCES);
        if (idPartida != null) {
            return avancePort.findActivosByPartida(idPartida);
        }
        if (idObra != null) {
            return avancePort.findActivosByObra(idObra);
        }
        return avancePort.findAllActivos();
    }

    @Override
    public ResultadoAvance registrar(UsuarioSesion actor, Integer idPartida, LocalDate fechaReporte,
                                     BigDecimal porcentajeAvance, String observaciones) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.REGISTRAR_AVANCES);
        if (idPartida == null || idPartida <= 0) {
            throw new ReglaNegocioException("Selecciona una partida válida.");
        }
        if (fechaReporte == null || fechaReporte.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha del avance no puede estar vacía ni ser futura.");
        }
        if (porcentajeAvance == null) {
            throw new ReglaNegocioException("El avance debe estar entre 0 y 100 %.");
        }
        BigDecimal porcentaje = porcentajeAvance.setScale(2, RoundingMode.HALF_UP);
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(CIEN_DEC) > 0) {
            throw new ReglaNegocioException("El avance debe estar entre 0 y 100 %.");
        }

        Partida partida = partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("La partida seleccionada no existe o está inactiva."));
        Obra obra = obraPort.findById(partida.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("Solo se puede registrar avance físico en partidas de obras activas (RF-05).");
        }

        Optional<AvancePartida> ultimoOpt = avancePort.findUltimoActivoByPartida(idPartida);
        if (ultimoOpt.isPresent()) {
            AvancePartida ultimo = ultimoOpt.get();
            if (fechaReporte.isBefore(ultimo.getFechaReporte())) {
                throw new ReglaNegocioException("La fecha no puede ser anterior al último avance registrado ("
                        + ultimo.getFechaReporte() + ").");
            }
            if (porcentaje.compareTo(ultimo.getPorcentajeAvance()) < 0) {
                throw new ReglaNegocioException("El avance acumulado no puede ser menor al último registrado ("
                        + ultimo.getPorcentajeAvance() + " %).");
            }
        }

        AvancePartida nuevo = new AvancePartida(
                null,
                idPartida,
                fechaReporte,
                porcentaje,
                observaciones == null || observaciones.isBlank() ? null : observaciones.trim(),
                actor.idUsuario(),
                true,
                null
        );
        AvancePartida guardado = avancePort.guardar(nuevo);
        BigDecimal avanceObra = calcularAvanceObra(obra.getIdObra());
        return new ResultadoAvance(guardado, avanceObra);
    }

    @Override
    public BigDecimal obtenerAvanceObra(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_AVANCES);
        obraPort.findById(idObra)
                .filter(Obra::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        return calcularAvanceObra(idObra);
    }

    private BigDecimal calcularAvanceObra(Integer idObra) {
        List<Partida> partidas = partidaPort.findActivasByObra(idObra);
        if (partidas.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        for (Partida p : partidas) {
            BigDecimal ultimo = avancePort.findUltimoActivoByPartida(p.getIdPartida())
                    .map(AvancePartida::getPorcentajeAvance)
                    .orElse(BigDecimal.ZERO);
            sumaPonderada = sumaPonderada.add(p.getPesoPresupuestal().multiply(ultimo));
        }
        return sumaPonderada.divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
