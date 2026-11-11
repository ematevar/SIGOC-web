package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.CierrePlanilla;
import com.sigoc.sigoc_web.domain.ports.out.CierrePlanillaPort;
import com.sigoc.sigoc_web.infraestructure.entities.CierrePlanillaEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.CierrePlanillaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CierrePlanillaJpaAdapter implements CierrePlanillaPort {

    private final CierrePlanillaJpaRepository repository;

    public CierrePlanillaJpaAdapter(CierrePlanillaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierrePlanilla> findAllActivos() {
        return repository.findByActivoTrueOrderByFechaCierreDescIdCierreDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierrePlanilla> findActivosByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByFechaCierreDescIdCierreDesc(idObra)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CierrePlanilla> findSolapado(Integer idObra, LocalDate desde, LocalDate hasta) {
        return repository.findPrimeroSolapado(idObra, desde, hasta).map(this::toModel);
    }

    @Override
    @Transactional
    public CierrePlanilla guardar(CierrePlanilla cierre) {
        CierrePlanillaEntity e = new CierrePlanillaEntity();
        e.setIdObra(cierre.getIdObra());
        e.setFechaInicioPeriodo(cierre.getFechaInicioPeriodo());
        e.setFechaFinPeriodo(cierre.getFechaFinPeriodo());
        e.setFechaCierre(cierre.getFechaCierre());
        e.setMontoTotal(cierre.getMontoTotal());
        e.setIdUsuario(cierre.getIdUsuario());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private CierrePlanilla toModel(CierrePlanillaEntity e) {
        return new CierrePlanilla(
                e.getIdCierre(),
                e.getIdObra(),
                e.getFechaInicioPeriodo(),
                e.getFechaFinPeriodo(),
                e.getFechaCierre(),
                e.getMontoTotal(),
                e.getIdUsuario(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
