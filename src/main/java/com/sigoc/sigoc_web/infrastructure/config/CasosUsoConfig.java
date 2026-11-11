package com.sigoc.sigoc_web.infrastructure.config;

import com.sigoc.sigoc_web.application.usecases.*;
import com.sigoc.sigoc_web.domain.ports.out.*;
import com.sigoc.sigoc_web.domain.services.CaptchaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class CasosUsoConfig {

    // ---------- Infraestructura compartida ----------

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }

    @Bean
    public CaptchaService captchaService() {
        return new CaptchaService();
    }

    // ---------- Seguridad y usuarios (RF-01, RF-02) ----------

    @Bean
    public ListarRoles listarRoles(RolPort port) {
        return new ListarRoles(port);
    }

    @Bean
    public GenerarCaptcha generarCaptcha(CaptchaService captcha) {
        return new GenerarCaptcha(captcha);
    }

    @Bean
    public IniciarSesion iniciarSesion(UsuarioPort usuarioPort, PasswordHasherPort hasher,
                                       IntentosLoginPort intentos, CaptchaService captcha, Clock clock) {
        return new IniciarSesion(usuarioPort, hasher, intentos, captcha, clock);
    }

    @Bean
    public CambiarClave cambiarClave(UsuarioPort usuarioPort, PasswordHasherPort hasher) {
        return new CambiarClave(usuarioPort, hasher);
    }

    @Bean
    public ListarUsuarios listarUsuarios(UsuarioPort usuarioPort) {
        return new ListarUsuarios(usuarioPort);
    }

    @Bean
    public CrearUsuario crearUsuario(UsuarioPort usuarioPort, RolPort rolPort, PasswordHasherPort hasher) {
        return new CrearUsuario(usuarioPort, rolPort, hasher);
    }

    @Bean
    public ActualizarUsuario actualizarUsuario(UsuarioPort usuarioPort, RolPort rolPort) {
        return new ActualizarUsuario(usuarioPort, rolPort);
    }

    @Bean
    public CambiarEstadoUsuario cambiarEstadoUsuario(UsuarioPort usuarioPort) {
        return new CambiarEstadoUsuario(usuarioPort);
    }

    // ---------- Obras y partidas (RF-03, RF-04, RF-16b) ----------

    @Bean
    public ListarObras listarObras(ObraPort obraPort) {
        return new ListarObras(obraPort);
    }

    @Bean
    public GuardarObra guardarObra(ObraPort obraPort) {
        return new GuardarObra(obraPort);
    }

    @Bean
    public EliminarObra eliminarObra(ObraPort obraPort) {
        return new EliminarObra(obraPort);
    }

    @Bean
    public CerrarObra cerrarObra(ObraPort obraPort) {
        return new CerrarObra(obraPort);
    }

    @Bean
    public ListarPartidas listarPartidas(PartidaPort partidaPort) {
        return new ListarPartidas(partidaPort);
    }

    @Bean
    public GuardarPartida guardarPartida(PartidaPort partidaPort, ObraPort obraPort) {
        return new GuardarPartida(partidaPort, obraPort);
    }

    @Bean
    public EliminarPartida eliminarPartida(PartidaPort partidaPort) {
        return new EliminarPartida(partidaPort);
    }

    @Bean
    public ListarMaterialesPartida listarMaterialesPartida(PartidaPort partidaPort,
                                                           PartidaMaterialPort partidaMaterialPort) {
        return new ListarMaterialesPartida(partidaPort, partidaMaterialPort);
    }

    @Bean
    public AsignarMaterialPartida asignarMaterialPartida(PartidaPort partidaPort, ObraPort obraPort,
                                                         PartidaMaterialPort partidaMaterialPort) {
        return new AsignarMaterialPartida(partidaPort, obraPort, partidaMaterialPort);
    }

    @Bean
    public EliminarMaterialPartida eliminarMaterialPartida(PartidaMaterialPort partidaMaterialPort) {
        return new EliminarMaterialPartida(partidaMaterialPort);
    }

    // ---------- Avances físicos (RF-05) ----------

    @Bean
    public ObtenerAvanceObra obtenerAvanceObra(ObraPort obraPort, PartidaPort partidaPort,
                                               AvancePartidaPort avancePort) {
        return new ObtenerAvanceObra(obraPort, partidaPort, avancePort);
    }

    @Bean
    public RegistrarAvance registrarAvance(AvancePartidaPort avancePort, PartidaPort partidaPort,
                                           ObraPort obraPort, ObtenerAvanceObra obtenerAvanceObra, Clock clock) {
        return new RegistrarAvance(avancePort, partidaPort, obraPort, obtenerAvanceObra, clock);
    }

    @Bean
    public ListarAvances listarAvances(AvancePartidaPort avancePort) {
        return new ListarAvances(avancePort);
    }

    // ---------- Materiales y proveedores (RF-06, RF-09) ----------

    @Bean
    public ListarMateriales listarMateriales(MaterialPort materialPort) {
        return new ListarMateriales(materialPort);
    }

    @Bean
    public ListarAlertasStock listarAlertasStock(MaterialPort materialPort) {
        return new ListarAlertasStock(materialPort);
    }

    @Bean
    public GuardarMaterial guardarMaterial(MaterialPort materialPort) {
        return new GuardarMaterial(materialPort);
    }

    @Bean
    public EliminarMaterial eliminarMaterial(MaterialPort materialPort) {
        return new EliminarMaterial(materialPort);
    }

    @Bean
    public ListarProveedores listarProveedores(ProveedorPort proveedorPort) {
        return new ListarProveedores(proveedorPort);
    }

    @Bean
    public GuardarProveedor guardarProveedor(ProveedorPort proveedorPort) {
        return new GuardarProveedor(proveedorPort);
    }

    @Bean
    public EliminarProveedor eliminarProveedor(ProveedorPort proveedorPort) {
        return new EliminarProveedor(proveedorPort);
    }

    // ---------- Compras y consumos (RF-07, RF-08) ----------

    @Bean
    public ListarCompras listarCompras(CompraPort compraPort) {
        return new ListarCompras(compraPort);
    }

    @Bean
    public RegistrarCompra registrarCompra(CompraPort compraPort, ProveedorPort proveedorPort,
                                           ObraPort obraPort, MaterialPort materialPort,
                                           EgresoPort egresoPort, Clock clock) {
        return new RegistrarCompra(compraPort, proveedorPort, obraPort, materialPort, egresoPort, clock);
    }

    @Bean
    public ListarConsumos listarConsumos(ConsumoPort consumoPort) {
        return new ListarConsumos(consumoPort);
    }

    @Bean
    public RegistrarConsumo registrarConsumo(ConsumoPort consumoPort, MaterialPort materialPort,
                                             PartidaPort partidaPort, ObraPort obraPort, Clock clock) {
        return new RegistrarConsumo(consumoPort, materialPort, partidaPort, obraPort, clock);
    }

    @Bean
    public ObtenerConsumoAcumuladoPartida obtenerConsumoAcumuladoPartida(ConsumoPort consumoPort) {
        return new ObtenerConsumoAcumuladoPartida(consumoPort);
    }

    // ---------- Personal, asistencia y producción (RF-10, RF-11, RF-12) ----------

    @Bean
    public ListarEmpleados listarEmpleados(EmpleadoPort empleadoPort) {
        return new ListarEmpleados(empleadoPort);
    }

    @Bean
    public GuardarEmpleado guardarEmpleado(EmpleadoPort empleadoPort) {
        return new GuardarEmpleado(empleadoPort);
    }

    @Bean
    public EliminarEmpleado eliminarEmpleado(EmpleadoPort empleadoPort) {
        return new EliminarEmpleado(empleadoPort);
    }

    @Bean
    public ListarAsistencias listarAsistencias(AsistenciaPort asistenciaPort) {
        return new ListarAsistencias(asistenciaPort);
    }

    @Bean
    public RegistrarAsistencia registrarAsistencia(AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                                                   ObraPort obraPort, Clock clock) {
        return new RegistrarAsistencia(asistenciaPort, empleadoPort, obraPort, clock);
    }

    @Bean
    public ListarProduccion listarProduccion(ProduccionPort produccionPort) {
        return new ListarProduccion(produccionPort);
    }

    @Bean
    public RegistrarProduccion registrarProduccion(ProduccionPort produccionPort, EmpleadoPort empleadoPort,
                                                   PartidaPort partidaPort, ObraPort obraPort,
                                                   AsistenciaPort asistenciaPort, Clock clock) {
        return new RegistrarProduccion(produccionPort, empleadoPort, partidaPort, obraPort, asistenciaPort, clock);
    }

    // --- Finanzas (Parte 7) ---

    @Bean
    public ListarIngresos listarIngresos(IngresoPort ingresoPort) {
        return new ListarIngresos(ingresoPort);
    }

    @Bean
    public RegistrarIngreso registrarIngreso(IngresoPort ingresoPort, ObraPort obraPort, Clock clock) {
        return new RegistrarIngreso(ingresoPort, obraPort, clock);
    }

    @Bean
    public ListarEgresos listarEgresos(EgresoPort egresoPort) {
        return new ListarEgresos(egresoPort);
    }

    @Bean
    public RegistrarEgreso registrarEgreso(EgresoPort egresoPort, ObraPort obraPort, Clock clock) {
        return new RegistrarEgreso(egresoPort, obraPort, clock);
    }

    @Bean
    public ListarCierresPlanilla listarCierresPlanilla(CierrePlanillaPort cierrePlanillaPort) {
        return new ListarCierresPlanilla(cierrePlanillaPort);
    }

    @Bean
    public CerrarPlanilla cerrarPlanilla(CierrePlanillaPort cierrePort, ObraPort obraPort,
                                         AsistenciaPort asistenciaPort, EmpleadoPort empleadoPort,
                                         EgresoPort egresoPort, Clock clock) {
        return new CerrarPlanilla(cierrePort, obraPort, asistenciaPort, empleadoPort, egresoPort, clock);
    }

    // --- Reportes (Parte 8) ---

    @Bean
    public GenerarReporte generarReporte(ReporteQueryPort reporteQueryPort) {
        return new GenerarReporte(reporteQueryPort);
    }

    @Bean
    public ExportarReporte exportarReporte(GenerarReporte generarReporte, ExportadorReportePort exportadorPort) {
        return new ExportarReporte(generarReporte, exportadorPort);
    }

    // --- Clientes, Valorizaciones, Comprobantes y Pagos ---

    @Bean
    public ListarClientes listarClientes(ClientePort clientePort) {
        return new ListarClientes(clientePort);
    }

    @Bean
    public GuardarCliente guardarCliente(ClientePort clientePort) {
        return new GuardarCliente(clientePort);
    }

    @Bean
    public EliminarCliente eliminarCliente(ClientePort clientePort) {
        return new EliminarCliente(clientePort);
    }

    @Bean
    public ListarValorizaciones listarValorizaciones(ValorizacionPort valorizacionPort) {
        return new ListarValorizaciones(valorizacionPort);
    }

    @Bean
    public RegistrarValorizacion registrarValorizacion(ValorizacionPort valorizacionPort, ObraPort obraPort, Clock clock) {
        return new RegistrarValorizacion(valorizacionPort, obraPort, clock);
    }

    @Bean
    public ListarComprobantes listarComprobantes(ComprobantePort comprobantePort) {
        return new ListarComprobantes(comprobantePort);
    }

    @Bean
    public EmitirComprobante emitirComprobante(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort, Clock clock) {
        return new EmitirComprobante(comprobantePort, valorizacionPort, clock);
    }

    @Bean
    public ListarPagos listarPagos(PagoPort pagoPort) {
        return new ListarPagos(pagoPort);
    }

    @Bean
    public ProcesarPago procesarPago(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                                     ComprobantePort comprobantePort, IngresoPort ingresoPort, Clock clock) {
        return new ProcesarPago(pagoPort, obraPort, valorizacionPort, comprobantePort, ingresoPort, clock);
    }
}
