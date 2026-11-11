package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.util.List;

/**
 * Lista pagos activos registrados en la pasarela sandbox.
 */
public class ListarPagos {
    private final PagoPort pagoPort;

    public ListarPagos(PagoPort pagoPort) {
        this.pagoPort = pagoPort;
    }

    public List<Pago> ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idObra != null && idObra > 0) {
            return pagoPort.findActivosByObra(idObra);
        }
        return pagoPort.findAllActivos();
    }
}
