package com.sigoc.sigoc_web.domain.ports.in;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;

import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.List;

/**
 * CU-17: Gestión de clientes contratantes con validación DNI/RUC.
 */
public interface GestionarClienteUseCase {
    @OperacionSoloLectura
    List<Cliente> listar(UsuarioSesion actor);

    Cliente guardar(UsuarioSesion actor, Integer idCliente, String tipoDocumento, String numeroDocumento,
                    String nombreRazonSocial, String telefono, String correo, String direccion);

    void eliminar(UsuarioSesion actor, Integer idCliente);
}
