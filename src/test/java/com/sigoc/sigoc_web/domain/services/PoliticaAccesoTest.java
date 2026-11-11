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
    void normalizaRolesHeredados() {
        assertEquals(RolSistema.RESIDENTE, RolSistema.desde("ingeniero").orElseThrow());
        assertFalse(RolSistema.esValido("CONTADOR"));
    }
}
