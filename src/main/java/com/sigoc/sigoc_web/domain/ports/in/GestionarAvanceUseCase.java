package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.AvancePartida;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-05: Registro y consulta de avances físicos por partida y avance ponderado de obra.
 */
public interface GestionarAvanceUseCase {

    record ResultadoAvance(AvancePartida avance, BigDecimal avanceObra) {
    }

    @OperacionSoloLectura
    List<AvancePartida> listar(UsuarioSesion actor, Integer idPartida, Integer idObra);

    ResultadoAvance registrar(UsuarioSesion actor, Integer idPartida, LocalDate fechaReporte,
                              BigDecimal porcentajeAvance, String observaciones);

    @OperacionSoloLectura
    BigDecimal obtenerAvanceObra(UsuarioSesion actor, Integer idObra);
}
