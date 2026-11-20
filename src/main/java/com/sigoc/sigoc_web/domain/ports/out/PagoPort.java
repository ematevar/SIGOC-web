package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.Pago;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoPort {
    List<Pago> findAllActivos();

    List<Pago> findActivosByObra(Integer idObra);

    Optional<Pago> findById(Integer idPago);

    Optional<Pago> findByCodigoOperacion(String codigoOperacion);

    default Optional<Pago> findByIdCapturaExterna(String idCapturaExterna) {
        return Optional.empty();
    }

    /**
     * Fechas de los intentos RECHAZADOS de un comprobante desde una fecha dada, en orden ascendente.
     */
    default List<LocalDateTime> findFechasRechazosDesde(Integer idComprobante, LocalDateTime desde) {
        return List.of();
    }

    Pago guardar(Pago pago);
}
