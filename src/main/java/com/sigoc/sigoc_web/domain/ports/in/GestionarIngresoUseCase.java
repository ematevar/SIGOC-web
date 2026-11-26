package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Ingreso;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-13: Registro y consulta de ingresos financieros por obra.
 */
public interface GestionarIngresoUseCase {
    List<Ingreso> listar(UsuarioSesion actor, Integer idObra);

    Ingreso registrar(UsuarioSesion actor, Integer idObra, LocalDate fecha, String concepto, BigDecimal monto);
}
