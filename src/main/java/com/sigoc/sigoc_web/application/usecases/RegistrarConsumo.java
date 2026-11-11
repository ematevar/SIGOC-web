package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.ConsumoPort;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PartidaPort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * RF-08: registra un consumo de material en una partida de obra activa, descuenta stock
 * y señala si el stock resultante quedó igual o por debajo del stock mínimo.
 */
public class RegistrarConsumo {

    private final ConsumoPort consumoPort;
    private final MaterialPort materialPort;
    private final PartidaPort partidaPort;
    private final ObraPort obraPort;
    private final Clock clock;

    public RegistrarConsumo(ConsumoPort consumoPort, MaterialPort materialPort,
                            PartidaPort partidaPort, ObraPort obraPort, Clock clock) {
        this.consumoPort = consumoPort;
        this.materialPort = materialPort;
        this.partidaPort = partidaPort;
        this.obraPort = obraPort;
        this.clock = clock;
    }

    public Resultado ejecutar(UsuarioSesion actor, Integer idMaterial, Integer idPartida,
                              LocalDate fechaConsumo, BigDecimal cantidad) {
        PoliticaAcceso.verificar(actor, RolSistema.RESIDENTE);
        if (idMaterial == null || idMaterial <= 0 || idPartida == null || idPartida <= 0) {
            throw new ReglaNegocioException("Selecciona material y partida.");
        }
        if (fechaConsumo == null || fechaConsumo.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de consumo no es válida ni puede ser futura.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
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
        Material material = materialPort.findById(idMaterial).filter(Material::isActivo).orElse(null);
        if (material == null) {
            throw new ReglaNegocioException("El material seleccionado no existe o está inactivo.");
        }

        BigDecimal cantBd = cantidad.setScale(2, RoundingMode.HALF_UP);
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
        return new Resultado(guardado, nuevoStock, alerta);
    }

    public record Resultado(ConsumoMaterial consumo, BigDecimal stockResultante, boolean alertaStockMinimo) {
    }
}
