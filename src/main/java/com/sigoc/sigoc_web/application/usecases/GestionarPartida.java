package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarPartidaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaMaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * CU-04: Gestión de partidas presupuestales y materiales asignados por partida.
 */
public class GestionarPartida implements GestionarPartidaUseCase {

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal PESO_MIN = new BigDecimal("0.01");

    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final PartidaMaterialPort partidaMaterialPort;

    public GestionarPartida(PartidaPort partidaPort, ObraPort obraPort, PartidaMaterialPort partidaMaterialPort) {
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.partidaMaterialPort = partidaMaterialPort;
    }

    @Override
    public List<Partida> listar(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PARTIDAS);
        return idObra != null ? partidaPort.findActivasByObra(idObra) : partidaPort.findAllActivas();
    }

    @Override
    public Partida guardar(UsuarioSesion actor, Integer idPartida, Integer idObra,
                           String nombre, String unidadMedida,
                           BigDecimal metradoPlanificado, BigDecimal pesoPresupuestal) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PARTIDAS);
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
        BigDecimal metradoBd = metradoPlanificado == null
                ? null
                : metradoPlanificado.setScale(2, RoundingMode.HALF_UP);
        if (metradoBd == null || metradoBd.compareTo(BigDecimal.ZERO) <= 0) {
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
            if (partidaPort.tieneMovimientos(idPartida)) {
                if (!idObra.equals(partida.getIdObra())) {
                    throw new ReglaNegocioException(
                            "No puedes mover una partida con avances, consumos o producción a otra obra.");
                }
                if (!unidadMedida.trim().equalsIgnoreCase(partida.getUnidadMedida().trim())) {
                    throw new ReglaNegocioException(
                            "No puedes cambiar la unidad de medida de una partida que ya registra movimientos.");
                }
            }
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
        partida.setMetradoPlanificado(metradoBd);
        partida.setPesoPresupuestal(peso);
        return partidaPort.guardar(partida);
    }

    @Override
    public void eliminar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PARTIDAS);
        if (idPartida == null || idPartida <= 0) {
            throw new ReglaNegocioException("Selecciona una partida válida.");
        }
        partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        if (partidaPort.tieneMovimientos(idPartida)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar una partida que ya registra avances, consumos o producción.");
        }
        partidaPort.desactivar(idPartida);
    }

    @Override
    public List<PartidaMaterial> listarMateriales(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PARTIDAS);
        partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        return partidaMaterialPort.findActivosByPartida(idPartida);
    }

    @Override
    public PartidaMaterial asignarMaterial(UsuarioSesion actor, Integer idPartida,
                                           Integer idMaterial, BigDecimal cantidadPresupuestada) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PARTIDAS);
        if (idPartida == null || idPartida <= 0 || idMaterial == null || idMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona una partida y un material válidos.");
        }
        BigDecimal cantBd = cantidadPresupuestada == null
                ? null
                : cantidadPresupuestada.setScale(2, RoundingMode.HALF_UP);
        if (cantBd == null || cantBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad presupuestada debe ser mayor que cero.");
        }
        Partida partida = partidaPort.findById(idPartida)
                .filter(Partida::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la partida."));
        Obra obra = obraPort.findById(partida.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("Solo se puede presupuestar materiales en partidas de una obra activa.");
        }
        if (!partidaMaterialPort.existeMaterialActivo(idMaterial)) {
            throw new RecursoNoEncontradoException("El material indicado no existe o está inactivo.");
        }
        return partidaMaterialPort.guardarOActualizar(idPartida, idMaterial, cantBd);
    }

    @Override
    public void eliminarMaterial(UsuarioSesion actor, Integer idPartidaMaterial) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.GESTIONAR_PARTIDAS);
        if (idPartidaMaterial == null || idPartidaMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona un registro válido.");
        }
        partidaMaterialPort.findById(idPartidaMaterial)
                .filter(PartidaMaterial::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el material presupuestado."));
        partidaMaterialPort.desactivar(idPartidaMaterial);
    }
}
