package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

/**
 * RF-05: registra un avance físico de partida (solo Residente de Obra) y devuelve
 * el nuevo avance físico acumulado ponderado de la obra.
 */
public class RegistrarAvance {

    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final AvancePartidaPort avancePort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final ObtenerAvanceObra obtenerAvanceObra;
    private final Clock clock;

    public RegistrarAvance(AvancePartidaPort avancePort, PartidaPort partidaPort, ObraPort obraPort,
                           ObtenerAvanceObra obtenerAvanceObra, Clock clock) {
        this.avancePort = avancePort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.obtenerAvanceObra = obtenerAvanceObra;
        this.clock = clock;
    }

    public Resultado ejecutar(UsuarioSesion actor, Integer idPartida, LocalDate fechaReporte,
                              BigDecimal porcentajeAvance, String observaciones) {
        PoliticaAcceso.verificar(actor, RolSistema.RESIDENTE);
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
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(CIEN) > 0) {
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
        BigDecimal avanceObra = obtenerAvanceObra.calcularSinPermisos(obra.getIdObra());
        return new Resultado(guardado, avanceObra);
    }

    public record Resultado(AvancePartida avance, BigDecimal avanceObra) {
    }
}
