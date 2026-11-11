package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TipoCambio {
    private Integer idTipoCambio;
    private String monedaOrigen;
    private String monedaDestino;
    private BigDecimal valor;
    private LocalDateTime fechaActualizacion;
    private Integer idUsuarioRegistro;

    public TipoCambio() {
    }

    public TipoCambio(Integer idTipoCambio, String monedaOrigen, String monedaDestino, BigDecimal valor,
                      LocalDateTime fechaActualizacion, Integer idUsuarioRegistro) {
        this.idTipoCambio = idTipoCambio;
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.valor = valor;
        this.fechaActualizacion = fechaActualizacion;
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    public Integer getIdTipoCambio() { return idTipoCambio; }
    public void setIdTipoCambio(Integer idTipoCambio) { this.idTipoCambio = idTipoCambio; }
    public String getMonedaOrigen() { return monedaOrigen; }
    public void setMonedaOrigen(String monedaOrigen) { this.monedaOrigen = monedaOrigen; }
    public String getMonedaDestino() { return monedaDestino; }
    public void setMonedaDestino(String monedaDestino) { this.monedaDestino = monedaDestino; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
}
