package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.services.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de los casos de uso CU-01, CU-02, CU-23 y CU-25 (Seguridad, Usuarios y Auditoría).
 */
class SeguridadUsuariosTest {

    private Fakes.UsuariosEnMemoria usuarios;
    private Fakes.RolesEnMemoria roles;
    private Fakes.HasherFalso hasher;
    private Fakes.IntentosEnMemoria intentos;
    private AuditoriaFake auditoriaPort;
    private MutableClock clock;
    private AutenticarUsuario autenticarUsuario;
    private GestionarUsuario gestionarUsuario;

    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion gerente = new UsuarioSesion(2, "gerente", "Gerente", RolSistema.GERENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(9, "alm", "Alm", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        usuarios = new Fakes.UsuariosEnMemoria();
        roles = new Fakes.RolesEnMemoria();
        hasher = new Fakes.HasherFalso();
        intentos = new Fakes.IntentosEnMemoria();
        auditoriaPort = new AuditoriaFake();
        clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        autenticarUsuario = new AutenticarUsuario(usuarios, hasher, intentos, new CaptchaService(), clock, auditoriaPort);
        gestionarUsuario = new GestionarUsuario(usuarios, roles, hasher, auditoriaPort, clock);
        usuarios.agregar("admin", "ADMIN", 1, "H:INICIAL:ClaveSegura123", true);
    }

    // ---------- CU-01 ----------

    @Test
    void loginRechazaCaptchaInvalidoYRegistraAuditoria() {
        var ex = assertThrows(ReglaNegocioException.class,
                () -> autenticarUsuario.iniciarSesion("admin", "ClaveSegura123", "ABC123", "WRONGC"));
        assertEquals("Captcha inválido.", ex.getMessage());
        assertEquals(1, auditoriaPort.eventos.size());
        assertEquals("LOGIN_FALLIDO", auditoriaPort.eventos.get(0).accion());
    }

    @Test
    void loginExitosoMarcaClaveInicial() {
        UsuarioSesion s = autenticarUsuario.iniciarSesion("admin", "ClaveSegura123", "ABC123", "abc123");
        assertEquals(RolSistema.ADMIN, s.rol());
        assertTrue(s.requiereCambioClave());
    }

    @Test
    void loginBloqueaTrasCincoFallosYDesbloqueaTras30s() {
        for (int i = 0; i < AutenticarUsuario.MAX_INTENTOS_FALLIDOS; i++) {
            final int n = i;
            assertThrows(ReglaNegocioException.class,
                    () -> autenticarUsuario.iniciarSesion("admin", "Mala" + n, "C", "C"));
        }
        var ex = assertThrows(ReglaNegocioException.class,
                () -> autenticarUsuario.iniciarSesion("admin", "ClaveSegura123", "C", "C"));
        assertTrue(ex.getMessage().startsWith("Demasiados intentos fallidos"));

        clock.avanzar(Duration.ofSeconds(31));
        assertNotNull(autenticarUsuario.iniciarSesion("admin", "ClaveSegura123", "C", "C"));
    }

    @Test
    void loginRechazaUsuarioInactivo() {
        usuarios.agregar("inactivo", "RESIDENTE", 2, "H:Clave12345", false);
        assertThrows(ReglaNegocioException.class,
                () -> autenticarUsuario.iniciarSesion("inactivo", "Clave12345", "C", "C"));
    }

    // ---------- CU-02 ----------

    @Test
    void cambiarClaveValidaActualYLongitud() {
        CambiarClave uc = new CambiarClave(usuarios, hasher, auditoriaPort, clock);
        UsuarioSesion sesion = new UsuarioSesion(1, "admin", "A", RolSistema.ADMIN, true);
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "ClaveSegura123", "corta"));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "ClaveSegura123", "ClaveSegura123"));
        assertThrows(ReglaNegocioException.class, () -> uc.ejecutar(sesion, "Incorrecta1", "NuevaClave99"));

        uc.ejecutar(sesion, "ClaveSegura123", "NuevaClave99");
        assertEquals("H:NuevaClave99", usuarios.findById(1).orElseThrow().getPasswordHash());
    }

    // ---------- CU-23 ----------

    @Test
    void crearUsuarioSoloAdmin() {
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarUsuario.crear(almacenero, "Juan", "juan", "Clave12345", 2));
        assertThrows(AccesoDenegadoException.class,
                () -> gestionarUsuario.crear(gerente, "Juan", "juan", "Clave12345", 2));
    }

    @Test
    void crearUsuarioValidaDatosRolYDuplicados() {
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.crear(admin, "Juan", "j", "Clave12345", 2));
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.crear(admin, "Juan", "juan", "corta", 2));
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.crear(admin, "Juan", "juan", "Clave12345", 99));
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.crear(admin, "Juan", "ADMIN", "Clave12345", 2));

        var creado = gestionarUsuario.crear(admin, " Juan Pérez ", "juan", "Clave12345", 2);
        assertEquals("Juan Pérez", creado.getNombreCompleto());
        assertEquals("H:Clave12345", creado.getPasswordHash());
        assertEquals("RESIDENTE", creado.getRol());

        var contador = gestionarUsuario.crear(admin, "Ana Torres", "contador1", "Clave12345", 4);
        assertEquals("CONTADOR", contador.getRol());
    }

    @Test
    void noPermiteDesactivarAlUnicoAdmin() {
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.cambiarEstado(admin, 1, false));

        usuarios.agregar("admin2", "ADMIN", 1, "H:x", true);
        gestionarUsuario.cambiarEstado(admin, 1, false);
        assertFalse(usuarios.findById(1).orElseThrow().isActivo());
    }

    @Test
    void noPermiteQuitarRolAlUnicoAdmin() {
        assertThrows(ReglaNegocioException.class, () -> gestionarUsuario.actualizar(admin, 1, "Admin", 2));
        gestionarUsuario.actualizar(admin, 1, "Admin Renombrado", 1);
        assertEquals("Admin Renombrado", usuarios.findById(1).orElseThrow().getNombreCompleto());
    }

    @Test
    void consultarAuditoriaExclusivaDeGerente() {
        ConsultarAuditoria consultar = new ConsultarAuditoria(auditoriaPort);
        autenticarUsuario.iniciarSesion("admin", "ClaveSegura123", "C", "C");

        assertThrows(AccesoDenegadoException.class, () -> consultar.ejecutar(admin, null, 20));
        assertThrows(AccesoDenegadoException.class, () -> consultar.ejecutar(almacenero, null, 20));
        assertEquals(1, consultar.ejecutar(gerente, null, 20).size());
    }

    static final class AuditoriaFake implements AuditoriaEventoPort {
        final List<AuditoriaEvento> eventos = new ArrayList<>();
        long seq = 1;

        @Override
        public AuditoriaEvento registrar(AuditoriaEvento e) {
            AuditoriaEvento guardado = new AuditoriaEvento(seq++, e.fechaHora(), e.idUsuario(), e.usuarioLogin(),
                    e.rol(), e.accion(), e.entidad(), e.idEntidad(), e.resultado(), e.detalle());
            eventos.add(guardado);
            return guardado;
        }

        @Override
        public List<AuditoriaEvento> listarRecientes(String accion, int limite) {
            return eventos.stream()
                    .filter(e -> accion == null || accion.isBlank() || accion.equalsIgnoreCase(e.accion()))
                    .limit(limite)
                    .toList();
        }
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
