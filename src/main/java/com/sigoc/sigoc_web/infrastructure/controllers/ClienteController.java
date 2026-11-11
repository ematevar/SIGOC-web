package com.sigoc.sigoc_web.infrastructure.controllers;

import com.sigoc.sigoc_web.application.usecases.EliminarCliente;
import com.sigoc.sigoc_web.application.usecases.GuardarCliente;
import com.sigoc.sigoc_web.application.usecases.ListarClientes;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.ClienteResponse;
import com.sigoc.sigoc_web.infrastructure.controllers.dto.FacturacionPagosDtos.GuardarClienteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de clientes contratantes con validación DNI/RUC y teléfono.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ListarClientes listarClientes;
    private final GuardarCliente guardarCliente;
    private final EliminarCliente eliminarCliente;

    public ClienteController(ListarClientes listarClientes, GuardarCliente guardarCliente,
                             EliminarCliente eliminarCliente) {
        this.listarClientes = listarClientes;
        this.guardarCliente = guardarCliente;
        this.eliminarCliente = eliminarCliente;
    }

    @GetMapping
    public List<ClienteResponse> listar(UsuarioSesion actor) {
        return listarClientes.ejecutar(actor).stream().map(ClienteResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(UsuarioSesion actor, @Valid @RequestBody GuardarClienteRequest body) {
        return ClienteResponse.de(guardarCliente.ejecutar(
                actor, null, body.tipoDocumento(), body.numeroDocumento(),
                body.nombreRazonSocial(), body.telefono(), body.correo(), body.direccion()));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(UsuarioSesion actor, @PathVariable Integer id,
                                      @Valid @RequestBody GuardarClienteRequest body) {
        return ClienteResponse.de(guardarCliente.ejecutar(
                actor, id, body.tipoDocumento(), body.numeroDocumento(),
                body.nombreRazonSocial(), body.telefono(), body.correo(), body.direccion()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(UsuarioSesion actor, @PathVariable Integer id) {
        eliminarCliente.ejecutar(actor, id);
        return ResponseEntity.noContent().build();
    }
}
