package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-03: Gestión del ciclo de vida de obras civiles y cierre formal.
 */
public interface GestionarObraUseCase {
    List<Obra> listar(UsuarioSesion actor);

    Obra guardar(UsuarioSesion actor, Integer idObra, Integer idCliente,
                 String nombre, String cliente, String ubicacion,
                 BigDecimal presupuestoTotal, LocalDate fechaInicioPlan,
                 LocalDate fechaFinPlan, String estado);

    void eliminar(UsuarioSesion actor, Integer idObra);

    void cerrar(UsuarioSesion actor, Integer idObra);
}
