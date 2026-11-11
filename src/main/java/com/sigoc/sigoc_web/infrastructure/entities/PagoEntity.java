package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class PagoEntity extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @Column(name = "id_obra", nullable = false)
    private Integer idObra;

    @Column(name = "id_valorizacion")
    private Integer idValorizacion;

    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @Column(name = "id_ingreso_generado")
    private Integer idIngresoGenerado;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodo_pago", nullable = false, length = 40)
    private String metodoPago;

    @Column(name = "codigo_operacion", nullable = false, unique = true, length = 50)
    private String codigoOperacion;

    @Column(name = "pasarela", nullable = false, length = 30)
    private String pasarela = "PASARELA_SIMULADA";

    @Column(name = "id_orden_externa", length = 80)
    private String idOrdenExterna;

    @Column(name = "id_captura_externa", unique = true, length = 80)
    private String idCapturaExterna;

    @Column(name = "monto_cobro", precision = 12, scale = 2)
    private BigDecimal montoCobro;

    @Column(name = "moneda_cobro", nullable = false, length = 3)
    private String monedaCobro = "PEN";

    @Column(name = "tipo_cambio", precision = 8, scale = 4)
    private BigDecimal tipoCambio;

    @Column(name = "marca_tarjeta", length = 20)
    private String marcaTarjeta;

    @Column(name = "ultimos4", length = 4)
    private String ultimos4;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado = "APROBADO";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public PagoEntity() {
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
