package com.sigoc.sigoc_web.domain.ports.out;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CierrePlanillaPort {
    List<CierrePlanilla> findAllActivos();

    List<CierrePlanilla> findActivosByObra(Integer idObra);

    Optional<CierrePlanilla> findSolapado(Integer idObra, LocalDate desde, LocalDate hasta);

    CierrePlanilla guardar(CierrePlanilla cierre);
}
