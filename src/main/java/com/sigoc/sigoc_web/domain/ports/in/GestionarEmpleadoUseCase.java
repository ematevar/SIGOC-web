package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-10: Gestión de trabajadores y jornal diario.
 */
public interface GestionarEmpleadoUseCase {
    List<Empleado> listar(UsuarioSesion actor);

    Empleado guardar(UsuarioSesion actor, Integer idEmpleado, String nombresApellidos,
                     String dni, String cargo, BigDecimal jornalDiario, LocalDate fechaIngreso);

    void eliminar(UsuarioSesion actor, Integer idEmpleado);
}
