package com.sigoc.sigoc_web.domain.models;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Material {
    private Integer idMaterial;
    private String nombre;
    private String unidadMedida;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal costoUnitarioRef;

    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Material() {
    }

    public Material(Integer idMaterial, String nombre, String unidadMedida, BigDecimal stockActual, BigDecimal stockMinimo, BigDecimal costoUnitarioRef, boolean activo, LocalDateTime fechaBorrado) {
        this.idMaterial = idMaterial;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.costoUnitarioRef = costoUnitarioRef;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(Integer idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getCostoUnitarioRef() {
        return costoUnitarioRef;
    }

    public void setCostoUnitarioRef(BigDecimal costoUnitarioRef) {
        this.costoUnitarioRef = costoUnitarioRef;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaBorrado() {
        return fechaBorrado;
    }

    public void setFechaBorrado(LocalDateTime fechaBorrado) {
        this.fechaBorrado = fechaBorrado;
    }

    /**
     * RF-06 / RF-08: indica si el stock actual alcanzó o cayó por debajo del mínimo configurado.
     */
    public boolean isStockEnAlerta() {
        return stockActual != null && stockMinimo != null && stockActual.compareTo(stockMinimo) <= 0;
    }
}
