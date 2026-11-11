package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.services.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de los casos de uso RF-01 / RF-02 (equivalentes a SigocSrsComplianceTest del desktop).
 */
class SeguridadUsuariosTest {

    private Fakes.UsuariosEnMemoria usuarios;
    private Fakes.RolesEnMemoria roles;
    private Fakes.HasherFalso hasher;
    private Fakes.IntentosEnMemoria intentos;
    private MutableClock clock;
    private IniciarSesion iniciarSesion;

    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(9, "alm", "Alm", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        usuarios = new Fakes.UsuariosEnMemoria();
        roles = new Fakes.RolesEnMemoria();
        hasher = new Fakes.HasherFalso();
        intentos = new Fakes.IntentosEnMemoria();
        clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        iniciarSesion = new IniciarSesion(usuarios, hasher, intentos, new CaptchaService(), clock);
        usuarios.agregar("admin", "ADMIN", 1, "H:INICIAL:ClaveSegura123", true);
    }

    // ---------- RF-01 ----------

    @Test
    void loginRechazaCaptchaInvalido() {
        var ex = assertThrows(ReglaNegocioException.class,
                () -> iniciarSesion.ejecutar("admin", "ClaveSegura123", "ABC123", "WRONGC"));
        assertEquals("Captcha inválido.", ex.getMessage());
    }

    @Test
    void loginExitosoMarcaClaveInicial() {
        UsuarioSesion s = iniciarSesion.ejecutar("admin", "ClaveSegura123", "ABC123", "abc123");
        assertEquals(RolSistema.ADMIN, s.rol());
        assertTrue(s.requiereCambioClave());
    }

    @Test
    void loginBloqueaTrasCincoFallosYDesbloqueaTras30s() {
        for (int i = 0; i < IniciarSesion.MAX_INTENTOS_FALLIDOS; i++) {
            final int n = i;
            assertThrows(ReglaNegocioException.class,
                    () -> iniciarSesion.ejecutar("admin", "Mala" + n, "C", "C"));
        }
        var ex = assertThrows(ReglaNegocioException.class,
                () -> iniciarSesion.ejecutar("admin", "ClaveSegura123", "C", "C"));
        assertTrue(ex.getMessage().startsWith("Demasiados intentos fallidos"));

        clock.avanzar(Duration.ofSeconds(31));
        assertNotNull(iniciarSesion.ejecutar("admin", "ClaveSegura123", "C", "C"));
    }

    @Test
    void loginRechazaUsuarioInactivo() {
        usuarios.agregar("inactivo", "RESIDENTE", 2, "H:Clave12345", false);
        assertThrows(ReglaNegocioException.class,
                () -> iniciarSesion.ejecutar("inactivo", "Clave12345", "C", "C"));
    }

    @Test
    void cambiarClaveValidaActualYLongitud() {
        CambiarClave uc = new CambiarClave(usuarios, hasher);
        UsuarioSesion sesion = new UsuarioSesion(1, "admin", "A", RolSistema.ADMIN, true);
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "ClaveSegura123", "corta"));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "ClaveSegura123", "ClaveSegura123"));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "Incorrecta1", "NuevaClave99"));

        uc.ejecutar(sesion, "ClaveSegura123", "NuevaClave99");
        assertEquals("H:NuevaClave99", usuarios.findById(1).orElseThrow().getPasswordHash());
    }

    // ---------- RF-02 ----------

    @Test
    void crearUsuarioSoloAdmin() {
        CrearUsuario uc = new CrearUsuario(usuarios, roles, hasher);
        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(almacenero, "Juan", "juan", "Clave12345", 2));
    }

    @Test
    void crearUsuarioValidaDatosRolYDuplicados() {
        CrearUsuario uc = new CrearUsuario(usuarios, roles, hasher);
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, "Juan", "j", "Clave12345", 2));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, "Juan", "juan", "corta", 2));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, "Juan", "juan", "Clave12345", 4));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, "Juan", "ADMIN", "Clave12345", 2));

        var creado = uc.ejecutar(admin, " Juan Pérez ", "juan", "Clave12345", 2);
        assertEquals("Juan Pérez", creado.getNombreCompleto());
        assertEquals("H:Clave12345", creado.getPasswordHash());
        assertEquals("RESIDENTE", creado.getRol());
    }

    @Test
    void noPermiteDesactivarAlUnicoAdmin() {
        CambiarEstadoUsuario uc = new CambiarEstadoUsuario(usuarios);
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, 1, false));

        usuarios.agregar("admin2", "ADMIN", 1, "H:x", true);
        uc.ejecutar(admin, 1, false);
        assertFalse(usuarios.findById(1).orElseThrow().isActivo());
    }

    @Test
    void noPermiteQuitarRolAlUnicoAdmin() {
        ActualizarUsuario uc = new ActualizarUsuario(usuarios, roles);
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(admin, 1, "Admin", 2));
        uc.ejecutar(admin, 1, "Admin Renombrado", 1);
        assertEquals("Admin Renombrado", usuarios.findById(1).orElseThrow().getNombreCompleto());
    }

    @Test
    void listarRolesExcluyeContador() {
        var lista = new ListarRoles(roles).ejecutar();
        assertEquals(3, lista.size());
        assertTrue(lista.stream().noneMatch(r -> r.getNombreRol().equals("CONTADOR")));
    }

    static final class MutableClock extends Clock {
        private Instant ahora;

        MutableClock(Instant inicio) {
            this.ahora = inicio;
        }

        void avanzar(Duration d) {
            ahora = ahora.plus(d);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
