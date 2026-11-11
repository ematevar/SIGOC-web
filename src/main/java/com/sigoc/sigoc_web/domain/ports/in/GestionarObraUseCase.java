package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GestionarObraUseCase {
    Obra ejecutar(UsuarioSesion actor, Integer idObra, Integer idCliente,
                  String nombre, String cliente, String ubicacion,
                  BigDecimal presupuestoTotal, LocalDate fechaInicioPlan,
                  LocalDate fechaFinPlan, String estado);
}
