package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarCompraUseCase;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.CompraDtos.CompraResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.CompraDtos.RegistrarCompraRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-07: Registro de compras de materiales (ALMACENERO).
 */
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final GestionarCompraUseCase gestionarCompra;

    public CompraController(GestionarCompraUseCase gestionarCompra) {
        this.gestionarCompra = gestionarCompra;
    }

    @GetMapping
    public List<CompraResponse> listar(UsuarioSesion actor) {
        return gestionarCompra.listar(actor).stream().map(CompraResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarCompraRequest body) {
        if (body.items() != null && !body.items().isEmpty()) {
            List<GestionarCompraUseCase.ItemCompra> items = body.items().stream()
                    .map(i -> new GestionarCompraUseCase.ItemCompra(i.idMaterial(), i.cantidad(), i.precioUnitario()))
                    .toList();
            return CompraResponse.de(gestionarCompra.registrarConItems(
                    actor,
                    body.idProveedor(),
                    body.idObra(),
                    body.tipoComprobante(),
                    body.numeroComprobante(),
                    body.fechaCompra(),
                    items
            ));
        }
        return CompraResponse.de(gestionarCompra.registrar(
                actor,
                body.idProveedor(),
                body.idObra(),
                body.idMaterial(),
                body.tipoComprobante(),
                body.numeroComprobante(),
                body.fechaCompra(),
                body.cantidad(),
                body.precioUnitario()
        ));
    }
}
