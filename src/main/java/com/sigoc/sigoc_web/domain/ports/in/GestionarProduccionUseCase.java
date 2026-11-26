package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.ProduccionEmpleado;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-12: Registro y consulta de producción diaria por trabajador y partida.
 */
public interface GestionarProduccionUseCase {
    @OperacionSoloLectura
    List<ProduccionEmpleado> listar(UsuarioSesion actor, Integer idPartida);

    ProduccionEmpleado registrar(UsuarioSesion actor, Integer idEmpleado, Integer idPartida,
                                 LocalDate fecha, BigDecimal cantidadProducida);
}
