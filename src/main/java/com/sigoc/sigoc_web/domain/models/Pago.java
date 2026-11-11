package com.sigoc.sigoc_web.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private Integer idPago;
    private Integer idObra;
    private Integer idValorizacion;
    private Integer idComprobante;
    private Integer idIngresoGenerado;
    private BigDecimal monto;
    private String metodoPago;
    private String codigoOperacion;
    private String pasarela = "PASARELA_SIMULADA";
    private String idOrdenExterna;
    private String idCapturaExterna;
    private BigDecimal montoCobro;
    private String monedaCobro = "PEN";
    private BigDecimal tipoCambio;
    private String marcaTarjeta;
    private String ultimos4;
    private String motivo;
    private LocalDateTime fechaPago;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaBorrado;

    public Pago() {
    }

    public Pago(Integer idPago, Integer idObra, Integer idValorizacion, Integer idComprobante,
                Integer idIngresoGenerado, BigDecimal monto, String metodoPago, String codigoOperacion,
                LocalDateTime fechaPago, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this(idPago, idObra, idValorizacion, idComprobante, idIngresoGenerado, monto, metodoPago,
                codigoOperacion, "PASARELA_SIMULADA", null, null, monto, "PEN", null, null, null, null,
                fechaPago, estado, activo, fechaBorrado);
    }

    public Pago(Integer idPago, Integer idObra, Integer idValorizacion, Integer idComprobante,
                Integer idIngresoGenerado, BigDecimal monto, String metodoPago, String codigoOperacion,
                String pasarela, String idOrdenExterna, String idCapturaExterna, BigDecimal montoCobro,
                String monedaCobro, BigDecimal tipoCambio, String marcaTarjeta, String ultimos4,
                String motivo, LocalDateTime fechaPago, String estado, boolean activo, LocalDateTime fechaBorrado) {
        this.idPago = idPago;
        this.idObra = idObra;
        this.idValorizacion = idValorizacion;
        this.idComprobante = idComprobante;
        this.idIngresoGenerado = idIngresoGenerado;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.codigoOperacion = codigoOperacion;
        this.pasarela = pasarela != null ? pasarela : "PASARELA_SIMULADA";
        this.idOrdenExterna = idOrdenExterna;
        this.idCapturaExterna = idCapturaExterna;
        this.montoCobro = montoCobro;
        this.monedaCobro = monedaCobro != null ? monedaCobro : "PEN";
        this.tipoCambio = tipoCambio;
        this.marcaTarjeta = marcaTarjeta;
        this.ultimos4 = ultimos4;
        this.motivo = motivo;
        this.fechaPago = fechaPago;
        this.estado = estado;
        this.activo = activo;
        this.fechaBorrado = fechaBorrado;
    }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public Integer getIdValorizacion() { return idValorizacion; }
    public void setIdValorizacion(Integer idValorizacion) { this.idValorizacion = idValorizacion; }
    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }
    public Integer getIdIngresoGenerado() { return idIngresoGenerado; }
    public void setIdIngresoGenerado(Integer idIngresoGenerado) { this.idIngresoGenerado = idIngresoGenerado; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }
    public String getPasarela() { return pasarela; }
    public void setPasarela(String pasarela) { this.pasarela = pasarela; }
    public String getIdOrdenExterna() { return idOrdenExterna; }
    public void setIdOrdenExterna(String idOrdenExterna) { this.idOrdenExterna = idOrdenExterna; }
    public String getIdCapturaExterna() { return idCapturaExterna; }
    public void setIdCapturaExterna(String idCapturaExterna) { this.idCapturaExterna = idCapturaExterna; }
    public BigDecimal getMontoCobro() { return montoCobro; }
    public void setMontoCobro(BigDecimal montoCobro) { this.montoCobro = montoCobro; }
    public String getMonedaCobro() { return monedaCobro; }
    public void setMonedaCobro(String monedaCobro) { this.monedaCobro = monedaCobro; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
    public String getMarcaTarjeta() { return marcaTarjeta; }
    public void setMarcaTarjeta(String marcaTarjeta) { this.marcaTarjeta = marcaTarjeta; }
    public String getUltimos4() { return ultimos4; }
    public void setUltimos4(String ultimos4) { this.ultimos4 = ultimos4; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaBorrado() { return fechaBorrado; }
    public void setFechaBorrado(LocalDateTime fechaBorrado) { this.fechaBorrado = fechaBorrado; }
}
