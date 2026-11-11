package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.ListarCompras;
import com.sigoc.sigoc_web.application.usecases.RegistrarCompra;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.CompraDtos.CompraResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.CompraDtos.RegistrarCompraRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-07: registro de compras de materiales (Almacenero y Administrador).
 */
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final ListarCompras listarCompras;
    private final RegistrarCompra registrarCompra;

    public CompraController(ListarCompras listarCompras, RegistrarCompra registrarCompra) {
        this.listarCompras = listarCompras;
        this.registrarCompra = registrarCompra;
    }

    @GetMapping
    public List<CompraResponse> listar(UsuarioSesion actor) {
        return listarCompras.ejecutar(actor).stream().map(CompraResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CompraResponse registrar(UsuarioSesion actor, @Valid @RequestBody RegistrarCompraRequest body) {
        return CompraResponse.de(registrarCompra.ejecutar(
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
