package com.sigoc.sigoc_web.infrastructure.config;

import com.sigoc.sigoc_web.application.usecases.*;
import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.ports.in.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Cableado de los 25 puertos de entrada (CU-01 a CU-25) con límites transaccionales
 * aplicados en la frontera de infraestructura para mantener domain y application
 * libres de dependencias de Spring/Jakarta.
 */
@Configuration
public class CasosUsoConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }

    @Bean
    public CaptchaService captchaService() {
        return new CaptchaService();
    }

    // ---------- Seguridad, Usuarios y Auditoría (CU-01, CU-02, CU-23, CU-25) ----------

    @Bean
    public AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioPort usuarioPort, PasswordHasherPort hasher,
                                                             IntentosLoginPort intentos, CaptchaService captcha,
                                                             Clock clock, AuditoriaEventoPort auditoriaPort,
                                                             PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(AutenticarUsuarioUseCase.class,
                new AutenticarUsuario(usuarioPort, hasher, intentos, captcha, clock, auditoriaPort), txManager);
    }

    @Bean
    public CambiarClaveUseCase cambiarClaveUseCase(UsuarioPort usuarioPort, PasswordHasherPort hasher,
                                                   AuditoriaEventoPort auditoriaPort, Clock clock,
                                                   PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(CambiarClaveUseCase.class,
                new CambiarClave(usuarioPort, hasher, auditoriaPort, clock), txManager, true);
    }

    @Bean
    public GestionarUsuarioUseCase gestionarUsuarioUseCase(UsuarioPort usuarioPort, RolPort rolPort,
                                                           PasswordHasherPort hasher, AuditoriaEventoPort auditoriaPort,
                                                           Clock clock, PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarUsuarioUseCase.class,
                new GestionarUsuario(usuarioPort, rolPort, hasher, auditoriaPort, clock), txManager);
    }

    @Bean
    public ConsultarAuditoriaUseCase consultarAuditoriaUseCase(AuditoriaEventoPort auditoriaPort,
                                                               PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(ConsultarAuditoriaUseCase.class,
                new ConsultarAuditoria(auditoriaPort), txManager);
    }

    // ---------- Clientes, Obras, Partidas y Avances (CU-03, CU-04, CU-05, CU-17) ----------

    @Bean
    public GestionarClienteUseCase gestionarClienteUseCase(ClientePort clientePort,
                                                           PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarClienteUseCase.class,
                new GestionarCliente(clientePort), txManager);
    }

    @Bean
    public GestionarObraUseCase gestionarObraUseCase(ObraPort obraPort, ClientePort clientePort,
                                                     AuditoriaEventoPort auditoriaPort, Clock clock,
                                                     PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarObraUseCase.class,
                new GestionarObra(obraPort, clientePort, auditoriaPort, clock), txManager);
    }

    @Bean
    public GestionarPartidaUseCase gestionarPartidaUseCase(PartidaPort partidaPort, ObraPort obraPort,
                                                           PartidaMaterialPort partidaMaterialPort,
                                                           PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarPartidaUseCase.class,
                new GestionarPartida(partidaPort, obraPort, partidaMaterialPort), txManager);
    }

    @Bean
    public GestionarAvanceUseCase gestionarAvanceUseCase(AvancePartidaPort avancePort, PartidaPort partidaPort,
                                                         ObraPort obraPort, Clock clock,
                                                         PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarAvanceUseCase.class,
                new GestionarAvance(avancePort, partidaPort, obraPort, clock), txManager);
    }

    // ---------- Logística: Materiales, Compras, Consumos y Proveedores (CU-06..CU-09) ----------

    @Bean
    public GestionarMaterialUseCase gestionarMaterialUseCase(MaterialPort materialPort,
                                                             PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarMaterialUseCase.class,
                new GestionarMaterial(materialPort), txManager);
    }

    @Bean
    public GestionarCompraUseCase gestionarCompraUseCase(CompraPort compraPort, ProveedorPort proveedorPort,
                                                         ObraPort obraPort, MaterialPort materialPort,
                                                         EgresoPort egresoPort, Clock clock,
                                                         PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarCompraUseCase.class,
                new GestionarCompra(compraPort, proveedorPort, obraPort, materialPort, egresoPort, clock), txManager);
    }

    @Bean
    public GestionarConsumoUseCase gestionarConsumoUseCase(ConsumoPort consumoPort, MaterialPort materialPort,
                                                           PartidaPort partidaPort, ObraPort obraPort, Clock clock,
                                                           PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarConsumoUseCase.class,
                new GestionarConsumo(consumoPort, materialPort, partidaPort, obraPort, clock), txManager);
    }

    @Bean
    public GestionarProveedorUseCase gestionarProveedorUseCase(ProveedorPort proveedorPort,
                                                               PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarProveedorUseCase.class,
                new GestionarProveedor(proveedorPort), txManager);
    }

    // ---------- Personal, Asistencia y Producción (CU-10..CU-12) ----------

    @Bean
    public GestionarEmpleadoUseCase gestionarEmpleadoUseCase(EmpleadoPort empleadoPort,
                                                             PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarEmpleadoUseCase.class,
                new GestionarEmpleado(empleadoPort), txManager);
    }

    @Bean
    public GestionarAsistenciaUseCase gestionarAsistenciaUseCase(AsistenciaPort asistenciaPort,
                                                                 EmpleadoPort empleadoPort, ObraPort obraPort,
                                                                 Clock clock, PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarAsistenciaUseCase.class,
                new GestionarAsistencia(asistenciaPort, empleadoPort, obraPort, clock), txManager);
    }

    @Bean
    public GestionarProduccionUseCase gestionarProduccionUseCase(ProduccionPort produccionPort,
                                                                 EmpleadoPort empleadoPort, PartidaPort partidaPort,
                                                                 ObraPort obraPort, AsistenciaPort asistenciaPort,
                                                                 Clock clock, PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarProduccionUseCase.class,
                new GestionarProduccion(produccionPort, empleadoPort, partidaPort, obraPort, asistenciaPort, clock),
                txManager);
    }

    // ---------- Finanzas, Planillas y Reportes (CU-13..CU-16) ----------

    @Bean
    public GestionarIngresoUseCase gestionarIngresoUseCase(IngresoPort ingresoPort, ObraPort obraPort,
                                                           Clock clock, PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarIngresoUseCase.class,
                new GestionarIngreso(ingresoPort, obraPort, clock), txManager);
    }

    @Bean
    public GestionarPlanillaUseCase gestionarPlanillaUseCase(CierrePlanillaPort cierrePort, ObraPort obraPort,
                                                             AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                                                             EgresoPort egresoPort, Clock clock,
                                                             AuditoriaEventoPort auditoriaPort,
                                                             PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarPlanillaUseCase.class,
                new GestionarPlanilla(cierrePort, obraPort, asistenciaPort, empleadoPort, egresoPort, clock, auditoriaPort),
                txManager);
    }

    @Bean
    public GestionarEgresoUseCase gestionarEgresoUseCase(EgresoPort egresoPort, ObraPort obraPort,
                                                         Clock clock, PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarEgresoUseCase.class,
                new GestionarEgreso(egresoPort, obraPort, clock), txManager);
    }

    @Bean
    public GenerarReporteUseCase generarReporteUseCase(ReporteQueryPort reporteQueryPort,
                                                       ExportadorReportePort exportadorPort,
                                                       PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GenerarReporteUseCase.class,
                new GestionarReporte(reporteQueryPort, exportadorPort), txManager);
    }

    // ---------- Valorizaciones, Comprobantes, Tipo de Cambio y Pagos (CU-18..CU-22, CU-24) ----------

    @Bean
    public GestionarValorizacionUseCase gestionarValorizacionUseCase(ValorizacionPort valorizacionPort,
                                                                     ObraPort obraPort, PartidaPort partidaPort,
                                                                     AvancePartidaPort avancePartidaPort, Clock clock,
                                                                     PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarValorizacionUseCase.class,
                new GestionarValorizacion(valorizacionPort, obraPort, partidaPort, avancePartidaPort, clock),
                txManager);
    }

    @Bean
    public GestionarComprobanteUseCase gestionarComprobanteUseCase(ComprobantePort comprobantePort,
                                                                   ValorizacionPort valorizacionPort,
                                                                   ObraPort obraPort, ClientePort clientePort,
                                                                   ExportadorComprobantePdfPort pdfPort, Clock clock,
                                                                   @Value("${sigoc.pago.enlace-horas-vigencia:72}") int horasVigencia,
                                                                   PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarComprobanteUseCase.class,
                new GestionarComprobante(comprobantePort, valorizacionPort, obraPort, clientePort, pdfPort, clock, horasVigencia),
                txManager);
    }

    @Bean
    public GestionarTipoCambioUseCase gestionarTipoCambioUseCase(TipoCambioPort tipoCambioPort, Clock clock,
                                                                 AuditoriaEventoPort auditoriaPort,
                                                                 PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(GestionarTipoCambioUseCase.class,
                new GestionarTipoCambio(tipoCambioPort, clock, auditoriaPort), txManager);
    }

    @Bean
    public ProcesarPago procesarPagoInterno(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                                            ComprobantePort comprobantePort, IngresoPort ingresoPort,
                                            TipoCambioPort tipoCambioPort, PasarelaPagoPort pasarelaPagoPort,
                                            Clock clock,
                                            @Value("${sigoc.pago.enlace-horas-vigencia:72}") int horasVigencia) {
        return new ProcesarPago(pagoPort, obraPort, valorizacionPort, comprobantePort, ingresoPort,
                tipoCambioPort, pasarelaPagoPort, clock, horasVigencia);
    }

    @Bean
    public GestionarCobranzaUseCase gestionarCobranzaUseCase(PagoPort pagoPort,
                                                             ProcesarPago procesarPagoInterno,
                                                             ComprobantePort comprobantePort,
                                                             ValorizacionPort valorizacionPort,
                                                             ObraPort obraPort, ClientePort clientePort,
                                                             TipoCambioPort tipoCambioPort,
                                                             PasarelaPagoPort pasarelaPagoPort, Clock clock,
                                                             @Value("${sigoc.pago.enlace-horas-vigencia:72}") int horasVigencia,
                                                             PlatformTransactionManager txManager) {
        ConsultarEnlacePagoPublico enlace = new ConsultarEnlacePagoPublico(
                comprobantePort, valorizacionPort, obraPort, clientePort,
                tipoCambioPort, pasarelaPagoPort, clock, pagoPort, horasVigencia);
        return TransaccionalProxy.envolver(GestionarCobranzaUseCase.class,
                new GestionarCobranza(pagoPort, procesarPagoInterno, enlace),
                txManager, PagoRechazadoException.class);
    }

    @Bean
    public ProcesarPagoExternoUseCase procesarPagoExternoUseCase(ProcesarPago procesarPagoInterno,
                                                                 PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(ProcesarPagoExternoUseCase.class,
                new ProcesarPagoExterno(procesarPagoInterno),
                txManager, PagoRechazadoException.class);
    }

    @Bean
    public ProcesarPagoTarjetaUseCase procesarPagoTarjetaUseCase(ProcesarPago procesarPagoInterno,
                                                                 PlatformTransactionManager txManager) {
        return TransaccionalProxy.envolver(ProcesarPagoTarjetaUseCase.class,
                new ProcesarPagoTarjeta(procesarPagoInterno),
                txManager, PagoRechazadoException.class);
    }
}
