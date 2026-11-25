package com.sigoc.sigoc_web.domain.services;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;

import java.util.EnumSet;
import java.util.Set;

/**
 * Política declarativa centralizada de autorización basada en roles
 * con segregación de funciones conforme a ISO/IEC 27001 A.5.3 (5 perfiles).
 */
public final class PoliticaAcceso {

    public enum Permiso {
        // TI / Seguridad (solo ADMIN)
        GESTIONAR_USUARIOS(RolSistema.ADMIN),

        // Auditoría inmutable (solo GERENTE)
        CONSULTAR_AUDITORIA(RolSistema.GERENTE),

        // Dirección de obras y clientes
        GESTIONAR_CLIENTES(RolSistema.GERENTE),
        VER_CLIENTES(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        GESTIONAR_OBRAS(RolSistema.GERENTE),
        VER_OBRAS(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR),

        // Ejecución técnica y campo
        GESTIONAR_PARTIDAS(RolSistema.GERENTE, RolSistema.RESIDENTE),
        VER_PARTIDAS(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR),
        REGISTRAR_AVANCES(RolSistema.RESIDENTE),
        VER_AVANCES(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        REGISTRAR_CONSUMOS(RolSistema.RESIDENTE),
        VER_CONSUMOS(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR),
        GESTIONAR_EMPLEADOS(RolSistema.GERENTE, RolSistema.RESIDENTE),
        VER_EMPLEADOS(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        REGISTRAR_ASISTENCIA(RolSistema.RESIDENTE),
        VER_ASISTENCIA(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        REGISTRAR_PRODUCCION(RolSistema.RESIDENTE),
        VER_PRODUCCION(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),

        // Logística y almacén
        GESTIONAR_MATERIALES(RolSistema.ALMACENERO),
        VER_MATERIALES(RolSistema.GERENTE, RolSistema.ALMACENERO, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        VER_ALERTAS_STOCK(RolSistema.GERENTE, RolSistema.ALMACENERO, RolSistema.RESIDENTE),
        REGISTRAR_COMPRAS(RolSistema.ALMACENERO),
        VER_COMPRAS(RolSistema.GERENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR),
        GESTIONAR_PROVEEDORES(RolSistema.GERENTE, RolSistema.ALMACENERO),
        VER_PROVEEDORES(RolSistema.GERENTE, RolSistema.ALMACENERO, RolSistema.CONTADOR),

        // Finanzas, planillas, facturación y pagos
        GESTIONAR_INGRESOS(RolSistema.CONTADOR),
        VER_INGRESOS(RolSistema.GERENTE, RolSistema.CONTADOR),
        GESTIONAR_PLANILLAS(RolSistema.CONTADOR),
        VER_PLANILLAS(RolSistema.GERENTE, RolSistema.CONTADOR),
        GESTIONAR_EGRESOS(RolSistema.CONTADOR),
        VER_EGRESOS(RolSistema.GERENTE, RolSistema.CONTADOR),
        GENERAR_REPORTES(RolSistema.GERENTE, RolSistema.CONTADOR),
        REGISTRAR_VALORIZACION(RolSistema.RESIDENTE, RolSistema.CONTADOR),
        VER_VALORIZACIONES(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        EMITIR_COMPROBANTE(RolSistema.CONTADOR),
        VER_COMPROBANTES(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR),
        GESTIONAR_TIPO_CAMBIO(RolSistema.CONTADOR),
        PROCESAR_PAGO(RolSistema.CONTADOR),
        VER_PAGOS(RolSistema.GERENTE, RolSistema.RESIDENTE, RolSistema.CONTADOR);

        private final Set<RolSistema> roles;

        Permiso(RolSistema primero, RolSistema... resto) {
            this.roles = EnumSet.of(primero, resto);
        }

        public Set<RolSistema> rolesPermitidos() {
            return Set.copyOf(roles);
        }
    }

    private PoliticaAcceso() {
    }

    public static void verificar(UsuarioSesion actor, Permiso permiso) {
        if (actor == null || actor.rol() == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación.");
        }
        if (permiso != null && permiso.roles.contains(actor.rol())) {
            return;
        }
        throw new AccesoDenegadoException("Acceso denegado: su perfil (" + actor.rol().getEtiqueta()
                + ") no tiene permisos para realizar esta operación.");
    }

    public static void verificar(UsuarioSesion actor, RolSistema... permitidos) {
        if (actor == null || actor.rol() == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación.");
        }
        for (RolSistema permitido : permitidos) {
            if (actor.rol() == permitido) {
                return;
            }
        }
        throw new AccesoDenegadoException("Acceso denegado: su perfil (" + actor.rol().getEtiqueta()
                + ") no tiene permisos para realizar esta operación.");
    }
}
