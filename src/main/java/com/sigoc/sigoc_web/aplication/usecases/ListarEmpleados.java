package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.models.Empleado;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.EmpleadoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * RF-10: lista los trabajadores activos.
 */
public class ListarEmpleados {
    private final EmpleadoPort empleadoPort;

    public ListarEmpleados(EmpleadoPort empleadoPort) {
        this.empleadoPort = empleadoPort;
    }

    public List<Empleado> ejecutar(UsuarioSesion actor) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        return empleadoPort.findAllActivos();
    }
}
