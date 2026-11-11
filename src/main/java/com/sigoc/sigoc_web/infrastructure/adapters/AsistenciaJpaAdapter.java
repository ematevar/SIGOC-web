package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.ports.out.AsistenciaPort;
import com.sigoc.sigoc_web.infrastructure.entities.AsistenciaEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.AsistenciaJpaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AsistenciaJpaAdapter implements AsistenciaPort {

    private final AsistenciaJpaRepository repository;
    private final EntityManager em;

    public AsistenciaJpaAdapter(AsistenciaJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asistencia> findAllActivas() {
        return repository.findByActivoTrueOrderByFechaDescIdAsistenciaDesc()
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asistencia> findActivasByObra(Integer idObra) {
        return repository.findByIdObraAndActivoTrueOrderByFechaDescIdAsistenciaDesc(idObra)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asistencia> findActivaByEmpleadoObraYFecha(Integer idEmpleado, Integer idObra, LocalDate fecha) {
        return repository.findFirstByIdEmpleadoAndIdObraAndFechaAndActivoTrue(idEmpleado, idObra, fecha)
                .map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asistencia> findActivasByObraYPeriodo(Integer idObra, LocalDate desde, LocalDate hasta) {
        return repository.findByIdObraAndFechaBetweenAndActivoTrue(idObra, desde, hasta)
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaEnPeriodoCerrado(Integer idObra, LocalDate fecha) {
        return !em.createNativeQuery(
                        "SELECT 1 FROM cierre_planilla WHERE id_obra = :idObra AND activo = 1 "
                                + "AND :fecha BETWEEN fecha_inicio_periodo AND fecha_fin_periodo")
                .setParameter("idObra", idObra)
                .setParameter("fecha", fecha)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    @Override
    @Transactional
    public Asistencia guardar(Asistencia asistencia) {
        AsistenciaEntity e = new AsistenciaEntity();
        e.setIdEmpleado(asistencia.getIdEmpleado());
        e.setIdObra(asistencia.getIdObra());
        e.setFecha(asistencia.getFecha());
        e.setHoraEntrada(asistencia.getHoraEntrada());
        e.setHoraSalida(asistencia.getHoraSalida());
        e.setEstado(asistencia.getEstado());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private Asistencia toModel(AsistenciaEntity e) {
        return new Asistencia(
                e.getIdAsistencia(),
                e.getIdEmpleado(),
                e.getIdObra(),
                e.getFecha(),
                e.getHoraEntrada(),
                e.getHoraSalida(),
                e.getEstado(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
