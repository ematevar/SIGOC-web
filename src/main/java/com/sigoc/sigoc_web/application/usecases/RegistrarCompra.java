package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * RF-07: registra una compra de uno o varios materiales incrementando stock y creando el egreso tipo Material.
 */
public class RegistrarCompra {

    public record ItemCompra(Integer idMaterial, BigDecimal cantidad, BigDecimal precioUnitario) {
    }

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
        return ejecutar(actor, idProveedor, idObra, tipoComprobante, numeroComprobante, fechaCompra,
                List.of(new ItemCompra(idMaterial, cantidad, precioUnitario)));
    }

    public CompraMaterial ejecutar(UsuarioSesion actor, Integer idProveedor, Integer idObra,
                                   String tipoComprobante, String numeroComprobante,
                                   LocalDate fechaCompra, List<ItemCompra> items) {
        PoliticaAcceso.verificar(actor, RolSistema.ALMACENERO, RolSistema.ADMIN);
        if (idProveedor == null || idProveedor <= 0 || idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona proveedor y obra.");
        }
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("Agrega al menos un material a la compra (RF-07).");
        }
        if (tipoComprobante == null || (!tipoComprobante.equals("Boleta") && !tipoComprobante.equals("Factura"))) {
            throw new ReglaNegocioException("El tipo de comprobante debe ser Boleta o Factura.");
        }
        if (numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new ReglaNegocioException("Ingresa el número de comprobante.");
        }
        if (fechaCompra == null || fechaCompra.isAfter(LocalDate.now(clock))) {
            throw new ReglaNegocioException("La fecha de compra no es válida ni puede ser futura.");
        }

        Proveedor proveedor = proveedorPort.findById(idProveedor).filter(Proveedor::isActivo).orElse(null);
        if (proveedor == null) {
            throw new ReglaNegocioException("El proveedor seleccionado no existe o se encuentra inactivo (RF-07).");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o no se encuentra activa (RF-07).");
        }

        String comprobanteLimpio = numeroComprobante.trim();
        if (compraPort.existsByProveedorYComprobante(idProveedor, comprobanteLimpio)) {
            throw new ReglaNegocioException("Ya existe una compra registrada con el comprobante "
                    + comprobanteLimpio + " para este proveedor.");
        }

        record ItemNormalizado(Integer idMaterial, BigDecimal cantidad, BigDecimal precioUnitario,
                               BigDecimal subtotal, Material material) {}

        // Ordenar por idMaterial al adquirir bloqueos para evitar interbloqueos.
        List<ItemCompra> ordenados = new ArrayList<>(items);
        ordenados.sort(Comparator.comparingInt(i -> i.idMaterial() == null ? 0 : i.idMaterial()));

        Set<Integer> materialesVistos = new HashSet<>();
        List<ItemNormalizado> normalizados = new ArrayList<>();
        BigDecimal totalBd = BigDecimal.ZERO;

        for (ItemCompra item : ordenados) {
            if (item == null || item.idMaterial() == null || item.idMaterial() <= 0) {
                throw new ReglaNegocioException("Selecciona un material válido en cada fila.");
            }
            if (!materialesVistos.add(item.idMaterial())) {
                throw new ReglaNegocioException("No repitas el mismo material dentro de una misma compra; suma su cantidad en una sola fila.");
            }
            BigDecimal cantBd = item.cantidad() == null ? null : item.cantidad().setScale(2, RoundingMode.HALF_UP);
            BigDecimal precioBd = item.precioUnitario() == null ? null : item.precioUnitario().setScale(2, RoundingMode.HALF_UP);
            if (cantBd == null || cantBd.compareTo(BigDecimal.ZERO) <= 0
                    || precioBd == null || precioBd.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ReglaNegocioException("Cantidad y precio unitario deben ser mayores que cero en todos los ítems.");
            }
            Material material = materialPort.findByIdParaActualizar(item.idMaterial())
                    .filter(Material::isActivo)
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los materiales seleccionados no existe o está inactivo."));
            BigDecimal subtotalBd = cantBd.multiply(precioBd).setScale(2, RoundingMode.HALF_UP);
            if (subtotalBd.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ReglaNegocioException("El subtotal de cada ítem debe ser mayor que cero.");
            }
            normalizados.add(new ItemNormalizado(item.idMaterial(), cantBd, precioBd, subtotalBd, material));
            totalBd = totalBd.add(subtotalBd);
        }

        totalBd = totalBd.setScale(2, RoundingMode.HALF_UP);
        if (totalBd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El total de la compra debe ser mayor que cero.");
        }

        CompraMaterial compra = new CompraMaterial(null, idProveedor, tipoComprobante,
                fechaCompra, comprobanteLimpio, totalBd, true, null);
        List<DetalleCompra> detalles = normalizados.stream()
                .map(n -> new DetalleCompra(null, null, n.idMaterial(), n.cantidad(), n.precioUnitario(), n.subtotal(), true, null))
                .toList();
        CompraMaterial guardada = compraPort.guardarCompraConDetalles(compra, detalles);

        for (ItemNormalizado n : normalizados) {
            BigDecimal nuevoStock = n.material().getStockActual().add(n.cantidad()).setScale(2, RoundingMode.HALF_UP);
            materialPort.actualizarStock(n.idMaterial(), nuevoStock, n.precioUnitario());
        }

        Egreso egreso = new Egreso(null, idObra, fechaCompra, "Material",
                "Compra " + tipoComprobante + " " + comprobanteLimpio,
                totalBd, guardada.getIdCompra(), null, true, null);
        egresoPort.guardar(egreso);
        return guardada;
    }
}
