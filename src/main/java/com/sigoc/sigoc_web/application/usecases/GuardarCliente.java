package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

/**
 * Crea o actualiza un cliente contratante validando DNI (8 dígitos) o RUC SUNAT (11 dígitos) y teléfono.
 */
public class GuardarCliente {
    private final ClientePort clientePort;

    public GuardarCliente(ClientePort clientePort) {
        this.clientePort = clientePort;
    }

    public Cliente ejecutar(UsuarioSesion actor, Integer idCliente, String tipoDocumento,
                           String numeroDocumento, String nombreRazonSocial,
                           String telefono, String correo, String direccion) {
        PoliticaAcceso.verificar(actor, RolSistema.ADMIN);
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            throw new ReglaNegocioException("Selecciona el tipo de documento (DNI o RUC).");
        }
        String tipo = tipoDocumento.trim().toUpperCase();
        String doc = numeroDocumento == null ? "" : numeroDocumento.trim();

        if ("DNI".equals(tipo)) {
            ValidadorSigoc.validarDni(doc);
            ValidadorSigoc.validarNombrePersona(nombreRazonSocial, "nombre completo del cliente");
        } else if ("RUC".equals(tipo)) {
            ValidadorSigoc.validarRuc(doc);
            if (nombreRazonSocial == null || nombreRazonSocial.isBlank()) {
                throw new ReglaNegocioException("La razón social del cliente es obligatoria.");
            }
        } else {
            throw new ReglaNegocioException("El tipo de documento solo puede ser DNI o RUC.");
        }

        ValidadorSigoc.validarTelefono(telefono);

        if (clientePort.existsByNumeroDocumento(doc, idCliente)) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el documento " + doc + ".");
        }

        Cliente cliente;
        if (idCliente != null) {
            cliente = clientePort.findById(idCliente)
                    .filter(Cliente::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el cliente."));
        } else {
            cliente = new Cliente();
            cliente.setActivo(true);
        }

        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(doc);
        cliente.setNombreRazonSocial(nombreRazonSocial.trim());
        cliente.setTelefono(telefono.trim());
        cliente.setCorreo(correo == null || correo.isBlank() ? null : correo.trim());
        cliente.setDireccion(direccion == null || direccion.isBlank() ? null : direccion.trim());
        return clientePort.guardar(cliente);
    }
}
