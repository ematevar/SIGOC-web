package com.sigoc.sigoc_web.domain.services;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaAccesoTest {

    private UsuarioSesion actor(RolSistema rol) {
        return new UsuarioSesion(1, "u", "Usuario", rol, false);
    }

    @Test
    void permiteRolAutorizado() {
        assertDoesNotThrow(() -> PoliticaAcceso.verificar(actor(RolSistema.ADMIN), RolSistema.ADMIN));
    }

    @Test
    void rechazaRolNoAutorizado() {
        var ex = assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.ALMACENERO), RolSistema.ADMIN, RolSistema.RESIDENTE));
        assertTrue(ex.getMessage().contains("Almacenero"));
    }

    @Test
    void rechazaSinSesion() {
        assertThrows(AccesoDenegadoException.class, () -> PoliticaAcceso.verificar(null, RolSistema.ADMIN));
    }

    @Test
    void normalizaRolesHeredadosYValidaCincoPerfilesIso27001() {
        assertEquals(RolSistema.RESIDENTE, RolSistema.desde("ingeniero").orElseThrow());
        assertEquals(RolSistema.CONTADOR, RolSistema.desde("contador").orElseThrow());
        assertEquals(RolSistema.GERENTE, RolSistema.desde("gerente").orElseThrow());
        assertTrue(RolSistema.esValido("CONTADOR"));
        assertTrue(RolSistema.esValido("GERENTE"));
        assertFalse(RolSistema.esValido("INVITADO"));
    }

    @Test
    void segregacionFuncionesIso27001AislaAdminYGerente() {
        assertDoesNotThrow(() -> PoliticaAcceso.verificar(actor(RolSistema.ADMIN), PoliticaAcceso.Permiso.GESTIONAR_USUARIOS));
        assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.ADMIN), PoliticaAcceso.Permiso.GESTIONAR_OBRAS));
        assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.ADMIN), PoliticaAcceso.Permiso.CONSULTAR_AUDITORIA));
        assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.ADMIN), PoliticaAcceso.Permiso.GESTIONAR_PLANILLAS));

        assertDoesNotThrow(() -> PoliticaAcceso.verificar(actor(RolSistema.GERENTE), PoliticaAcceso.Permiso.CONSULTAR_AUDITORIA));
        assertDoesNotThrow(() -> PoliticaAcceso.verificar(actor(RolSistema.GERENTE), PoliticaAcceso.Permiso.GESTIONAR_OBRAS));
        assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.GERENTE), PoliticaAcceso.Permiso.GESTIONAR_USUARIOS));
        assertThrows(AccesoDenegadoException.class,
                () -> PoliticaAcceso.verificar(actor(RolSistema.GERENTE), PoliticaAcceso.Permiso.PROCESAR_PAGO));
    }
}
