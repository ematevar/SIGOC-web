package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarEmpleadoUseCase;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-10: Gestión de trabajadores con validación de DNI único y jornal diario positivo.
 */
public class GestionarEmpleado implements GestionarEmpleadoUseCase {

    private final EmpleadoPort empleadoPort;

    public GestionarEmpleado(EmpleadoPort empleadoPort) {
        this.empleadoPort = empleadoPort;
    }

    @Override
    public List<Empleado> listar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_EMPLEADOS);
        return empleadoPort.findAllActivos();
    }

    @Override
    public Empleado guardar(UsuarioSesion actor, Integer idEmpleado, String nombresApellidos,
                            String dni, String cargo, BigDecimal jornalDiario, LocalDate fechaIngreso) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_EMPLEADOS);
        if (nombresApellidos == null || nombresApellidos.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: nombres_apellidos.");
        }
        String dniLimpio = dni == null ? "" : dni.trim();
        ValidadorSigoc.validarDni(dniLimpio);
        if (empleadoPort.existsByDni(dniLimpio, idEmpleado)) {
            throw new ReglaNegocioException("Ya existe un trabajador registrado con el DNI " + dniLimpio + " (RF-10).");
        }
        if (cargo == null || cargo.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: cargo.");
        }
        BigDecimal jornalBd = jornalDiario == null ? null : jornalDiario.setScale(2, RoundingMode.HALF_UP);
        if (jornalBd == null || jornalBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El campo jornal_diario debe ser mayor que cero.");
        }
        if (fechaIngreso == null) {
            throw new ReglaNegocioException("Completa el campo obligatorio: fecha_ingreso.");
        }

        Empleado empleado;
        if (idEmpleado != null) {
            empleado = empleadoPort.findById(idEmpleado)
                    .filter(Empleado::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trabajador."));
        } else {
            empleado = new Empleado();
            empleado.setActivo(true);
        }

        empleado.setNombresApellidos(nombresApellidos.trim());
        empleado.setDni(dniLimpio);
        empleado.setCargo(cargo.trim());
        empleado.setJornalDiario(jornalBd);
        empleado.setFechaIngreso(fechaIngreso);
        return empleadoPort.guardar(empleado);
    }

    @Override
    public void eliminar(UsuarioSesion actor, Integer idEmpleado) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_EMPLEADOS);
        if (idEmpleado == null || idEmpleado <= 0) {
            throw new ReglaNegocioException("Selecciona un trabajador válido.");
        }
        empleadoPort.findById(idEmpleado)
                .filter(Empleado::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trabajador."));
        empleadoPort.desactivar(idEmpleado);
    }
}
