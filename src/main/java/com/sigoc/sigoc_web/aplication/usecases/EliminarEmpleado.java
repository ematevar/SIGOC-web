package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-10: da de baja lógica a un trabajador.
 */
public class EliminarEmpleado {
    private final EmpleadoPort empleadoPort;

    public EliminarEmpleado(EmpleadoPort empleadoPort) {
        this.empleadoPort = empleadoPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idEmpleado) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idEmpleado == null || idEmpleado <= 0) {
            throw new ReglaNegocioException("Selecciona un trabajador válido.");
        }
        empleadoPort.findById(idEmpleado)
                .filter(Empleado::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el trabajador."));
        empleadoPort.desactivar(idEmpleado);
    }
}
