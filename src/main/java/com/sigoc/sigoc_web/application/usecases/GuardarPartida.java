package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * RF-04: crea o actualiza una partida en una obra activa controlando que la suma de
 * pesos presupuestales de la obra no supere el 100 %.
 */
public class GuardarPartida {

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal PESO_MIN = new BigDecimal("0.01");

    private final PartidaPort partidaPort;
    private final ObraPort obraPort;

    public GuardarPartida(PartidaPort partidaPort, ObraPort obraPort) {
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
    }

    public Partida ejecutar(UsuarioSesion actor, Integer idPartida, Integer idObra,
                            String nombre, String unidadMedida,
                            BigDecimal metradoPlanificado, BigDecimal pesoPresupuestal) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN, RolSistema.RESIDENTE);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("El campo id_obra debe ser mayor que cero.");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("Solo se pueden registrar o editar partidas en una obra activa (RF-04).");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: nombre.");
        }
        if (unidadMedida == null || unidadMedida.isBlank()) {
            throw new ReglaNegocioException("Completa el campo obligatorio: unidad_medida.");
        }
        if (metradoPlanificado == null || metradoPlanificado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El campo metrado_planificado debe ser mayor que cero.");
        }
        if (pesoPresupuestal == null) {
            throw new ReglaNegocioException("El campo peso_presupuestal es obligatorio.");
        }
        BigDecimal peso = pesoPresupuestal.setScale(2, RoundingMode.HALF_UP);
        if (peso.compareTo(PESO_MIN) < 0 || peso.compareTo(CIEN) > 0) {
            throw new ReglaNegocioException("El campo peso_presupuestal debe estar entre 0.01 y 100.0.");
        }

        Partida partida;
        if (idPartida != null) {
            partida = partidaPort.findById(idPartida)
                    .filter(Partida::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        } else {
            partida = new Partida();
            partida.setActivo(true);
        }

        BigDecimal sumaExistente = partidaPort.sumaPesosActivos(idObra, idPartida);
        if (sumaExistente.add(peso).compareTo(CIEN) > 0) {
            BigDecimal disponible = CIEN.subtract(sumaExistente).max(BigDecimal.ZERO);
            throw new ReglaNegocioException(String.format(Locale.US,
                    "La suma de pesos presupuestales de la obra no puede superar el 100 %% (RF-04). Acumulado actual: %.2f %%, disponible: %.2f %%.",
                    sumaExistente.doubleValue(), disponible.doubleValue()));
        }

        partida.setIdObra(idObra);
        partida.setNombre(nombre.trim());
        partida.setUnidadMedida(unidadMedida.trim());
        partida.setMetradoPlanificado(metradoPlanificado.setScale(2, RoundingMode.HALF_UP));
        partida.setPesoPresupuestal(peso);
        return partidaPort.guardar(partida);
    }
}
