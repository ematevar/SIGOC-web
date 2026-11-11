package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Registra una valorización de obra en estado inicial PENDIENTE.
 */
public class RegistrarValorizacion {
    private final ValorizacionPort valorizacionPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public RegistrarValorizacion(ValorizacionPort valorizacionPort, ObraPort obraPort, Clock clock) {
        this.valorizacionPort = valorizacionPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    public Valorizacion ejecutar(UsuarioSesion actor, Integer idObra, String periodo,
                                LocalDate fechaEmision, BigDecimal porcentajeAvancePeriodo,
                                BigDecimal montoValorizado) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida.");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o se encuentra cerrada.");
        }

        ValidadorSigoc.validarPeriodo(periodo);
        ValidadorSigoc.validarPorcentaje(porcentajeAvancePeriodo, "avance del periodo");
        ValidadorSigoc.validarMontoPositivo(montoValorizado, "valorización");

        LocalDate fecha = fechaEmision != null ? fechaEmision : LocalDate.now(clock);
        if (fecha.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de emisión no puede ser futura.");
        }

        Valorizacion val = new Valorizacion();
        val.setIdObra(idObra);
        val.setPeriodo(periodo.trim());
        val.setFechaEmision(fecha);
        val.setPorcentajeAvancePeriodo(porcentajeAvancePeriodo.setScale(2, RoundingMode.HALF_UP));
        val.setMontoValorizado(montoValorizado.setScale(2, RoundingMode.HALF_UP));
        val.setEstado("PENDIENTE");
        val.setActivo(true);
        return valorizacionPort.guardar(val);
    }
}
