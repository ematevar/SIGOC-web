package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Asistencia;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CU-11 / RF-11: registra en lote la asistencia diaria de una cuadrilla para una obra y fecha.
 */
public class RegistrarAsistenciaLote {

    public record FilaAsistencia(Integer idEmpleado, String estado, LocalTime horaEntrada, LocalTime horaSalida) {
    }

    private final RegistrarAsistencia registrarAsistencia;

    public RegistrarAsistenciaLote(RegistrarAsistencia registrarAsistencia) {
        this.registrarAsistencia = registrarAsistencia;
    }

    public List<Asistencia> ejecutar(UsuarioSesion actor, Integer idObra, LocalDate fecha, List<FilaAsistencia> filas) {
        PoliticaAcceso.verificar(actor, RolSistema.RESIDENTE);
        if (filas == null || filas.isEmpty()) {
            throw new ReglaNegocioException("Incluye al menos un trabajador para registrar asistencia.");
        }
        List<Asistencia> registradas = new ArrayList<>(filas.size());
        for (FilaAsistencia fila : filas) {
            registradas.add(registrarAsistencia.ejecutar(
                    actor,
                    fila.idEmpleado(),
                    idObra,
                    fecha,
                    fila.horaEntrada(),
                    fila.horaSalida(),
                    fila.estado()
            ));
        }
        return registradas;
    }
}
