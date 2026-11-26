package com.sigoc.sigoc_web.infrastructure.security;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;
import com.sigoc.sigoc_web.domain.models.RolSistema;
import com.sigoc.sigoc_web.domain.models.Usuario;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.ports.out.UsuarioPort;
import com.sigoc.sigoc_web.infrastructure.config.TransaccionalProxy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica los controles de seguridad de infraestructura v3.1:
 * - CAPTCHA en imagen PNG sin texto plano en el payload.
 * - Protección de /actuator/metrics, /swagger-ui y /v3/api-docs para solo ADMIN.
 * - TransaccionalProxy basado en @OperacionSoloLectura (sin convención de nombres).
 */
class SeguridadInfraestructuraTest {

    @Test
    void captchaRendererGeneraPngValidoSinTextoPlano() {
        CaptchaImagenRenderer renderer = new CaptchaImagenRenderer();
        String dataUri = renderer.renderizarPngDataUri("A7K9M2");
        assertTrue(dataUri.startsWith("data:image/png;base64,"));
        assertFalse(dataUri.contains("A7K9M2"));
        byte[] bytes = Base64.getDecoder().decode(dataUri.substring("data:image/png;base64,".length()));
        // Firma mágica PNG: 89 50 4E 47
        assertTrue(bytes.length > 100);
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 0x50, bytes[1]);
        assertEquals((byte) 0x4E, bytes[2]);
        assertEquals((byte) 0x47, bytes[3]);
    }

    @Test
    void interceptorProtegeActuatorYSwaggerSoloParaAdminYDejaHealthPublico() throws Exception {
        UsuarioPort stubUsuarios = new UsuarioPort() {
            @Override public List<Usuario> findAll() { return List.of(); }
            @Override public Optional<Usuario> findById(Integer id) { return Optional.empty(); }
            @Override public Optional<Usuario> findHabilitadoByLogin(String login) {
                Usuario u = new Usuario();
                u.setIdUsuario("admin".equals(login) ? 1 : 2);
                u.setUsername(login);
                u.setRol("admin".equals(login) ? "ADMIN" : "GERENTE");
                u.setActivo(true);
                return Optional.of(u);
            }
            @Override public boolean existsByLogin(String login) { return false; }
            @Override public long contarAdminsActivos() { return 1; }
            @Override public Usuario crear(Usuario usuario) { return usuario; }
            @Override public void actualizarDatos(Integer idUsuario, String nombreCompleto, Integer idRol) {}
            @Override public void cambiarEstado(Integer idUsuario, boolean activo) {}
            @Override public void actualizarClave(Integer idUsuario, String passwordHash) {}
        };
        AutenticacionInterceptor interceptor = new AutenticacionInterceptor(stubUsuarios);

        // /actuator/health público sin sesión
        MockHttpServletRequest reqHealth = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse resHealth = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(reqHealth, resHealth, new Object()));

        // /actuator/metrics y /v3/api-docs sin sesión -> 401
        for (String ruta : List.of("/actuator/metrics", "/actuator/info", "/v3/api-docs", "/swagger-ui/index.html")) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", ruta);
            MockHttpServletResponse res = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(req, res, new Object()), "Debe bloquear sin sesión: " + ruta);
            assertEquals(401, res.getStatus(), "Debe devolver 401 en " + ruta);
        }

        // GERENTE (no ADMIN) intentando ver /actuator/metrics -> 403
        MockHttpServletRequest reqGerente = new MockHttpServletRequest("GET", "/actuator/metrics");
        SesionHttp.guardarUsuario(reqGerente, new UsuarioSesion(2, "gerente", "Gerente", RolSistema.GERENTE, false));
        MockHttpServletResponse resGerente = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(reqGerente, resGerente, new Object()));
        assertEquals(403, resGerente.getStatus());

        // ADMIN accediendo a /actuator/metrics y /v3/api-docs -> permitido
        MockHttpServletRequest reqAdmin = new MockHttpServletRequest("GET", "/v3/api-docs");
        SesionHttp.guardarUsuario(reqAdmin, new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false));
        MockHttpServletResponse resAdmin = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(reqAdmin, resAdmin, new Object()));
    }

    public interface PuertoPruebaTx {
        String listarQueEnRealidadEscribe();

        @OperacionSoloLectura
        String consultaExplicita();
    }

    public static class PuertoPruebaTxImpl implements PuertoPruebaTx {
        @Override public String listarQueEnRealidadEscribe() { return "ok-write"; }
        @Override public String consultaExplicita() { return "ok-read"; }
    }

    @Test
    void transaccionalProxyUsaAnotacionExplicitaYNoPrefijoDeNombre() {
        AtomicBoolean ultimoReadOnly = new AtomicBoolean(false);
        PlatformTransactionManager txManager = new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                ultimoReadOnly.set(definition.isReadOnly());
                return new SimpleTransactionStatus();
            }
            @Override public void commit(TransactionStatus status) {}
            @Override public void rollback(TransactionStatus status) {}
        };

        PuertoPruebaTx proxy = TransaccionalProxy.envolver(PuertoPruebaTx.class, new PuertoPruebaTxImpl(), txManager);

        // Aunque empiece con "listar", NO tiene @OperacionSoloLectura -> DEBE ser readOnly=false
        assertEquals("ok-write", proxy.listarQueEnRealidadEscribe());
        assertFalse(ultimoReadOnly.get(), "Método sin @OperacionSoloLectura debe ser lectura-escritura");

        // Con @OperacionSoloLectura -> readOnly=true
        assertEquals("ok-read", proxy.consultaExplicita());
        assertTrue(ultimoReadOnly.get(), "Método con @OperacionSoloLectura debe ser solo lectura");
    }
}
