package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.models.AvancePartida;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.AvancePartidaPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * RF-05: calcula el avance físico acumulado ponderado de una obra
 * sumando (peso_presupuestal * último porcentaje activo) / 100.
 */
public class ObtenerAvanceObra {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ObraPort obraPort;
    private final PartidaPort partidaPort;
    private final AvancePartidaPort avancePort;

    public ObtenerAvanceObra(ObraPort obraPort, PartidaPort partidaPort, AvancePartidaPort avancePort) {
        this.obraPort = obraPort;
        this.partidaPort = partidaPort;
        this.avancePort = avancePort;
    }

    public BigDecimal ejecutar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE, RolSistema.CONTADOR);
        obraPort.findById(idObra)
                .filter(Obra::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la obra."));
        return calcularSinPermisos(idObra);
    }

    BigDecimal calcularSinPermisos(Integer idObra) {
        List<Partida> partidas = partidaPort.findActivasByObra(idObra);
        if (partidas.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        for (Partida p : partidas) {
            BigDecimal ultimo = avancePort.findUltimoActivoByPartida(p.getIdPartida())
                    .map(AvancePartida::getPorcentajeAvance)
                    .orElse(BigDecimal.ZERO);
            sumaPonderada = sumaPonderada.add(p.getPesoPresupuestal().multiply(ultimo));
        }
        return sumaPonderada.divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
