package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

/**
 * RF-03: da de baja lógica una obra siempre que no tenga partidas ni movimientos asociados.
 */
public class EliminarObra {
    private final ObraPort obraPort;

    public EliminarObra(ObraPort obraPort) {
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
        if (obraPort.tieneMovimientos(idObra)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar una obra que tiene partidas o movimientos registrados (RF-03). Utilice la opción de cerrar/finalizar obra.");
        }
        obraPort.desactivar(idObra);
    }
}
