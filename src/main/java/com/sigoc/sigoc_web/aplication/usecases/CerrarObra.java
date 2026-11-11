package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-03: marca como Finalizada una obra para conservar su historial sin permitir nuevos registros.
 */
public class CerrarObra {
    private final ObraPort obraPort;

    public CerrarObra(ObraPort obraPort) {
        this.obraPort = obraPort;
    }

    public void ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida.");
        }
        obraPort.findById(idObra)
                .filter(Obra::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        obraPort.cerrar(idObra);
    }
}
