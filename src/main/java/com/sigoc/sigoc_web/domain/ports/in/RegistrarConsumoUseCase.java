package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegistrarConsumoUseCase {
    Object ejecutar(UsuarioSesion actor, Integer idMaterial, Integer idPartida,
                    LocalDate fechaConsumo, BigDecimal cantidad);
}
