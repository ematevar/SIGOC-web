package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegistrarAvanceUseCase {
    Object ejecutar(UsuarioSesion actor, Integer idPartida, LocalDate fechaReporte,
                    BigDecimal porcentajeAvance, String observaciones);
}
