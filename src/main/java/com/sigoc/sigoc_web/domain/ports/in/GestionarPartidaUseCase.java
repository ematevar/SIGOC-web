package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Partida;
import com.sigoc.sigoc_web.domain.models.PartidaMaterial;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.math.BigDecimal;
import java.util.List;

/**
 * CU-04: Gestión de partidas presupuestales y materiales asignados por partida.
 */
public interface GestionarPartidaUseCase {
    @OperacionSoloLectura
    List<Partida> listar(UsuarioSesion actor, Integer idObra);

    Partida guardar(UsuarioSesion actor, Integer idPartida, Integer idObra,
                    String nombre, String unidadMedida,
                    BigDecimal metradoPlanificado, BigDecimal pesoPresupuestal);

    void eliminar(UsuarioSesion actor, Integer idPartida);

    @OperacionSoloLectura
    List<PartidaMaterial> listarMateriales(UsuarioSesion actor, Integer idPartida);

    PartidaMaterial asignarMaterial(UsuarioSesion actor, Integer idPartida,
                                    Integer idMaterial, BigDecimal cantidadPresupuestada);

    void eliminarMaterial(UsuarioSesion actor, Integer idPartidaMaterial);
}
