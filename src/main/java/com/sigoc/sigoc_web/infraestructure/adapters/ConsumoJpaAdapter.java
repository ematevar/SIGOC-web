package com.sigoc.sigoc_web.infraestructure.adapters;

import com.sigoc.sigoc_web.domain.models.ConsumoMaterial;
import com.sigoc.sigoc_web.domain.ports.out.ConsumoPort;
import com.sigoc.sigoc_web.infraestructure.entities.ConsumoMaterialEntity;
import com.sigoc.sigoc_web.infraestructure.repositories.ConsumoMaterialJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ConsumoJpaAdapter implements ConsumoPort {

    private final ConsumoMaterialJpaRepository repository;

    public ConsumoJpaAdapter(ConsumoMaterialJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumoMaterial> findAllActivos() {
        return repository.findByActivoTrueOrderByFechaConsumoDescIdConsumoDesc()
                .stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumoMaterial> findActivosByPartida(Integer idPartida) {
        return repository.findByIdPartidaAndActivoTrueOrderByFechaConsumoDescIdConsumoDesc(idPartida)
                .stream().map(this::toModel).toList();
    }

    @Override
    public BigDecimal sumaCantidadPorPartidaYMaterial(Integer idPartida, Integer idMaterial) {
        BigDecimal sum = repository.sumaCantidadPorPartidaYMaterial(idPartida, idMaterial);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    @Transactional
    public ConsumoMaterial guardar(ConsumoMaterial consumo) {
        ConsumoMaterialEntity e = new ConsumoMaterialEntity();
        e.setIdPartida(consumo.getIdPartida());
        e.setIdMaterial(consumo.getIdMaterial());
        e.setFechaConsumo(consumo.getFechaConsumo());
        e.setCantidad(consumo.getCantidad());
        e.setIdUsuario(consumo.getIdUsuario());
        e.setActivo(true);
        return toModel(repository.saveAndFlush(e));
    }

    private ConsumoMaterial toModel(ConsumoMaterialEntity e) {
        return new ConsumoMaterial(
                e.getIdConsumo(),
                e.getIdPartida(),
                e.getIdMaterial(),
                e.getFechaConsumo(),
                e.getCantidad(),
                e.getIdUsuario(),
                e.isActivo(),
                e.getFechaBorrado()
        );
    }
}
