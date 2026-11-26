package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-08: Registro de consumos de materiales por partida y control de stock mínimo.
 */
public interface GestionarConsumoUseCase {

    record ResultadoConsumo(ConsumoMaterial consumo, BigDecimal stockResultante, boolean alertaStockMinimo) {
    }

    @OperacionSoloLectura
    List<ConsumoMaterial> listar(UsuarioSesion actor, Integer idPartida);

    ResultadoConsumo registrar(UsuarioSesion actor, Integer idMaterial, Integer idPartida,
                               LocalDate fechaConsumo, BigDecimal cantidad);

    @OperacionSoloLectura
    BigDecimal obtenerAcumulado(UsuarioSesion actor, Integer idPartida, Integer idMaterial);
}
