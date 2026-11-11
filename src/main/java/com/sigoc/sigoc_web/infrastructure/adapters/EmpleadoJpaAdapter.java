package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.infrastructure.entities.EmpleadoEntity;
import com.sigoc.sigoc_web.infrastructure.repositories.EmpleadoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EmpleadoJpaAdapter implements EmpleadoPort {

    private final EmpleadoJpaRepository repository;

    public EmpleadoJpaAdapter(EmpleadoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empleado> findAllActivos() {
        return repository.findByActivoTrueOrderByIdEmpleadoDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empleado> findById(Integer idEmpleado) {
        return repository.findById(idEmpleado).map(this::toModel);
    }

    @Override
    public boolean existsByDni(String dni, Integer idExcluir) {
        return repository.existsByDni(dni, idExcluir);
    }

    @Override
    @Transactional
    public Empleado guardar(Empleado empleado) {
        EmpleadoEntity e = empleado.getIdEmpleado() == null
                ? new EmpleadoEntity()
                : repository.findById(empleado.getIdEmpleado())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trabajador."));
        e.setNombresApellidos(empleado.getNombresApellidos());
        e.setDni(empleado.getDni());
        e.setCargo(empleado.getCargo());
        e.setJornalDiario(empleado.getJornalDiario());
        e.setFechaIngreso(empleado.getFechaIngreso());
        e.setActivo(empleado.isActivo());
        return toModel(repository.saveAndFlush(e));
    }

    @Override
    @Transactional
    public void desactivar(Integer idEmpleado) {
        EmpleadoEntity e = repository.findById(idEmpleado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trabajador."));
        e.setActivo(false);
        e.setFechaBorrado(LocalDateTime.now());
    }

    private Empleado toModel(EmpleadoEntity e) {
        return new Empleado(
                e.getIdEmpleado(),
                e.getNombresApellidos(),
                e.getDni(),
                e.getCargo(),
                e.getJornalDiario(),
                e.getFechaIngreso(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
