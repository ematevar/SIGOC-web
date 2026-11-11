package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * RF-07: registra una compra de material incrementando stock y creando el egreso tipo Material.
 */
public class RegistrarCompra {

    private final CompraPort compraPort;
    private final ProveedorPort proveedorPort;
    private final ObraPort obraPort;
    private final MaterialPort materialPort;
    private final EgresoPort egresoPort;
    private final Clock clock;

    public RegistrarCompra(CompraPort compraPort, ProveedorPort proveedorPort, ObraPort obraPort,
                           MaterialPort materialPort, EgresoPort egresoPort, Clock clock) {
        this.compraPort = compraPort;
        this.proveedorPort = proveedorPort;
        this.obraPort = obraPort;
        this.materialPort = materialPort;
        this.egresoPort = egresoPort;
        this.clock = clock;
    }

    public CompraMaterial ejecutar(UsuarioSesion actor, Integer idProveedor, Integer idObra,
                                  Integer idMaterial, String tipoComprobante, String numeroComprobante,
                                  LocalDate fechaCompra, BigDecimal cantidad, BigDecimal precioUnitario) {
        PoliticaAcceso.verificar(actor, RolSistema.ALMACENERO, RolSistema.ADMIN);
        if (idProveedor == null || idProveedor <= 0 || idObra == null || idObra <= 0
                || idMaterial == null || idMaterial <= 0) {
            throw new ReglaNegocioException("Selecciona proveedor, obra y material.");
        }
        if (tipoComprobante == null || (!tipoComprobante.equals("Boleta") && !tipoComprobante.equals("Factura"))) {
            throw new ReglaNegocioException("El tipo de comprobante debe ser Boleta o Factura.");
        }
        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new ReglaNegocioException("Ingresa el número de comprobante.");
        }
        if (fechaCompra == null || fechaCompra.isAfter(LocalDate.now(clock))
                || cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0
                || precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Fecha, cantidad o precio no válidos.");
        }

        Proveedor proveedor = proveedorPort.findById(idProveedor).filter(Proveedor::isActivo).orElse(null);
        if (proveedor == null) {
            throw new ReglaNegocioException("El proveedor seleccionado no existe o se encuentra inactivo (RF-07).");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o no se encuentra activa (RF-07).");
        }
        Material material = materialPort.findById(idMaterial).filter(Material::isActivo).orElse(null);
        if (material == null) {
            throw new ReglaNegocioException("El material seleccionado no existe o está inactivo.");
        }

        String comprobanteLimpio = numeroComprobante.trim();
        if (compraPort.existsByProveedorYComprobante(idProveedor, comprobanteLimpio)) {
            throw new ReglaNegocioException("Ya existe una compra registrada con el comprobante "
                    + comprobanteLimpio + " para este proveedor.");
        }

        BigDecimal cantBd = cantidad.setScale(2, RoundingMode.HALF_UP);
        BigDecimal precioBd = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBd = cantBd.multiply(precioBd).setScale(2, RoundingMode.HALF_UP);
        if (totalBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El total de la compra debe ser mayor que cero.");
        }

        CompraMaterial compra = new CompraMaterial(null, idProveedor, tipoComprobante,
                fechaCompra, comprobanteLimpio, totalBd, true, null);
        DetalleCompra detalle = new DetalleCompra(null, null, idMaterial, cantBd, precioBd, totalBd, true, null);
        CompraMaterial guardada = compraPort.guardarCompraConDetalle(compra, detalle);

        BigDecimal nuevoStock = material.getStockActual().add(cantBd).setScale(2, RoundingMode.HALF_UP);
        materialPort.actualizarStock(idMaterial, nuevoStock, precioBd);

        Egreso egreso = new Egreso(null, idObra, fechaCompra, "Material",
                "Compra " + tipoComprobante + " " + comprobanteLimpio,
                totalBd, guardada.getIdCompra(), null, true, null);
        egresoPort.guardar(egreso);
        return guardada;
    }
}
