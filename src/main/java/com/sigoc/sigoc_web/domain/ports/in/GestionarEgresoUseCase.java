package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Egreso;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-15: Registro y consulta de egresos financieros por obra.
 */
public interface GestionarEgresoUseCase {
    List<Egreso> listar(UsuarioSesion actor, Integer idObra);

    Egreso registrar(UsuarioSesion actor, Integer idObra, LocalDate fecha, String concepto, BigDecimal monto);
}
