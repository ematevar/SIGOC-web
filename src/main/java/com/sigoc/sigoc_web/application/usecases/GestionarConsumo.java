package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;
import com.sigoc.sigoc_web.domain.models.Material;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarConsumoUseCase;
import com.sigoc.sigoc_web.domain.ports.out.ConsumoPort;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-08: Registro de consumos de materiales por partida y control de stock mínimo.
 */
public class GestionarConsumo implements GestionarConsumoUseCase {

    private final ConsumoPort consumoPort;
    private final MaterialPort materialPort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public GestionarConsumo(ConsumoPort consumoPort, MaterialPort materialPort,
                            PartidaPort partidaPort, ObraPort obraPort, Clock clock) {
        this.consumoPort = consumoPort;
        this.materialPort = materialPort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    @Override
    public List<ConsumoMaterial> listar(UsuarioSesion actor, Integer idPartida) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_CONSUMOS);
        return idPartida != null ? consumoPort.findActivosByPartida(idPartida) : consumoPort.findAllActivos();
    }

    @Override
    public ResultadoConsumo registrar(UsuarioSesion actor, Integer idMaterial, Integer idPartida,
                                      LocalDate fechaConsumo, BigDecimal cantidad) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.REGISTRAR_CONSUMOS);
        if (idMaterial == null || idMaterial <= 0 || idPartida == null || idPartida <= 0) {
            throw new ReglaNegocioException("Selecciona material y partida.");
        }
        if (fechaConsumo == null || fechaConsumo.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de consumo no es válida ni puede ser futura.");
        }
        BigDecimal cantBd = cantidad == null ? null : cantidad.setScale(2, RoundingMode.HALF_UP);
        if (cantBd == null || cantBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }

        Partida partida = partidaPort.findById(idPartida).filter(Partida::isActivo).orElse(null);
        if (partida == null) {
            throw new ReglaNegocioException("La partida seleccionada no existe o está inactiva.");
        }
        Obra obra = obraPort.findById(partida.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("Solo se puede registrar consumo de materiales en partidas de una obra activa (RF-08).");
        }
        Material material = materialPort.findByIdParaActualizar(idMaterial).filter(Material::isActivo).orElse(null);
        if (material == null) {
            throw new ReglaNegocioException("El material seleccionado no existe o está inactivo.");
        }

        BigDecimal stockDisponible = material.getStockActual();
        if (stockDisponible.compareTo(cantBd) < 0) {
            throw new ReglaNegocioException("Stock insuficiente. Disponible: " + stockDisponible);
        }

        ConsumoMaterial consumo = new ConsumoMaterial(
                null, idPartida, idMaterial, fechaConsumo, cantBd, actor.idUsuario(), true, null);
        ConsumoMaterial guardado = consumoPort.guardar(consumo);

        BigDecimal nuevoStock = stockDisponible.subtract(cantBd).setScale(2, RoundingMode.HALF_UP);
        materialPort.actualizarStock(idMaterial, nuevoStock, null);

        boolean alerta = material.getStockMinimo() != null
                && nuevoStock.compareTo(material.getStockMinimo()) <= 0;
        return new ResultadoConsumo(guardado, nuevoStock, alerta);
    }

    @Override
    public BigDecimal obtenerAcumulado(UsuarioSesion actor, Integer idPartida, Integer idMaterial) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_CONSUMOS);
        return consumoPort.sumaCantidadPorPartidaYMaterial(idPartida, idMaterial);
    }
}
