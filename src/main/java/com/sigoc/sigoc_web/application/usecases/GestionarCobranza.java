package com.sigoc.sigoc_web.application.usecases;

import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.AuditoriaEvento;
import com.sigoc.sigoc_web.domain.models.Cliente;
import com.sigoc.sigoc_web.domain.models.Comprobante;
import com.sigoc.sigoc_web.domain.models.Ingreso;
import com.sigoc.sigoc_web.domain.models.Obra;
import com.sigoc.sigoc_web.domain.models.Pago;
import com.sigoc.sigoc_web.domain.models.TipoCambio;
import com.sigoc.sigoc_web.domain.models.UsuarioSesion;
import com.sigoc.sigoc_web.domain.models.Valorizacion;
import com.sigoc.sigoc_web.domain.ports.in.GestionarCobranzaUseCase;
import com.sigoc.sigoc_web.domain.ports.out.AuditoriaEventoPort;
import com.sigoc.sigoc_web.domain.ports.out.ClientePort;
import com.sigoc.sigoc_web.domain.ports.out.ComprobantePort;
import com.sigoc.sigoc_web.domain.ports.out.IngresoPort;
import com.sigoc.sigoc_web.domain.ports.out.ObraPort;
import com.sigoc.sigoc_web.domain.ports.out.PagoPort;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoExternaPort;
import com.sigoc.sigoc_web.domain.ports.out.TipoCambioPort;
import com.sigoc.sigoc_web.domain.ports.out.ValorizacionPort;
import com.sigoc.sigoc_web.domain.services.ControlIntentosPago;
import com.sigoc.sigoc_web.domain.services.PoliticaAcceso;
import com.sigoc.sigoc_web.domain.validators.ValidadorSigoc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * CU-22: Gestión de cobranzas, registro interno de pagos y consulta pública del enlace de 72 horas.
 */
public class GestionarCobranza implements GestionarCobranzaUseCase {

    private final PagoPort pagoPort;
    private final ObraPort obraPort;
    private final ValorizacionPort valorizacionPort;
    private final ComprobantePort comprobantePort;
    private final IngresoPort ingresoPort;
    private final ClientePort clientePort;
    private final TipoCambioPort tipoCambioPort;
    private final PasarelaPagoExternaPort pasarelaExternaPort;
    private final Clock clock;
    private final int horasVigenciaEnlace;
    private final AuditoriaEventoPort auditoriaPort;

    public GestionarCobranza(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                             ComprobantePort comprobantePort, IngresoPort ingresoPort, ClientePort clientePort,
                             TipoCambioPort tipoCambioPort, PasarelaPagoExternaPort pasarelaExternaPort,
                             Clock clock, int horasVigenciaEnlace) {
        this(pagoPort, obraPort, valorizacionPort, comprobantePort, ingresoPort, clientePort,
                tipoCambioPort, pasarelaExternaPort, clock, horasVigenciaEnlace, null);
    }

    public GestionarCobranza(PagoPort pagoPort, ObraPort obraPort, ValorizacionPort valorizacionPort,
                             ComprobantePort comprobantePort, IngresoPort ingresoPort, ClientePort clientePort,
                             TipoCambioPort tipoCambioPort, PasarelaPagoExternaPort pasarelaExternaPort,
                             Clock clock, int horasVigenciaEnlace, AuditoriaEventoPort auditoriaPort) {
        this.pagoPort = pagoPort;
        this.obraPort = obraPort;
        this.valorizacionPort = valorizacionPort;
        this.comprobantePort = comprobantePort;
        this.ingresoPort = ingresoPort;
        this.clientePort = clientePort;
        this.tipoCambioPort = tipoCambioPort;
        this.pasarelaExternaPort = pasarelaExternaPort;
        this.clock = clock;
        this.horasVigenciaEnlace = Math.max(1, horasVigenciaEnlace);
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    public List<Pago> listarPagos(UsuarioSesion actor, Integer idObra) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.VER_PAGOS);
        return idObra != null ? pagoPort.findActivosByObra(idObra) : pagoPort.findAllActivos();
    }

    @Override
    public Pago registrarPagoInterno(UsuarioSesion actor, Integer idObra, Integer idValorizacion,
                                     Integer idComprobante, BigDecimal monto,
                                     String metodoPago, String codigoOperacion) {
        PoliticaAcceso.verificar(actor, PoliticaAcceso.Permiso.PROCESAR_PAGO);
        if (idObra == null || idObra <= 0) {
            throw new ReglaNegocioException("Selecciona una obra válida.");
        }
        Obra obra = obraPort.findById(idObra).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra seleccionada no existe o se encuentra cerrada.");
        }

        ValidadorSigoc.validarMontoPositivo(monto, "pago");
        BigDecimal montoEscala = monto.setScale(2, RoundingMode.HALF_UP);

        String metodo = (metodoPago == null || metodoPago.isBlank())
                ? "PASARELA_SIMULADA" : metodoPago.trim().toUpperCase();
        String codigoOp = resolverCodigoOperacion(codigoOperacion, "SIM-");

        Comprobante comp = null;
        Valorizacion val = null;
        String conceptoIngreso = "Pago aprobado (" + metodo + " - Op: " + codigoOp + ")";

        if (idComprobante != null && idComprobante > 0) {
            comp = comprobantePort.findByIdParaActualizar(idComprobante)
                    .filter(Comprobante::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El comprobante asociado no existe."));
            if (!comp.esPagable()) {
                throw new ReglaNegocioException("El comprobante no está en estado pagable (actual: " + comp.getEstado() + ").");
            }
            if (idValorizacion != null && idValorizacion > 0 && !Objects.equals(comp.getIdValorizacion(), idValorizacion)) {
                throw new ReglaNegocioException("El comprobante no corresponde a la valorización indicada.");
            }
            val = valorizacionPort.findByIdParaActualizar(comp.getIdValorizacion())
                    .filter(Valorizacion::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada al comprobante no existe."));
            if (!Objects.equals(val.getIdObra(), idObra)) {
                throw new ReglaNegocioException("El comprobante pertenece a otra obra.");
            }
            if (val.estaPagada()) {
                throw new ReglaNegocioException("La valorización asociada ya se encuentra pagada.");
            }
            if (montoEscala.compareTo(comp.getTotal().setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new ReglaNegocioException(
                        "El monto del pago (S/ " + montoEscala + ") debe coincidir exactamente con el comprobante (S/ "
                                + comp.getTotal() + ").");
            }
            conceptoIngreso = "Pago Comprobante " + comp.getSerieNumero() + " (" + codigoOp + ")";
        } else if (idValorizacion != null && idValorizacion > 0) {
            val = valorizacionPort.findByIdParaActualizar(idValorizacion)
                    .filter(Valorizacion::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
            if (!Objects.equals(val.getIdObra(), idObra)) {
                throw new ReglaNegocioException("La valorización no pertenece a la obra seleccionada.");
            }
            if (val.estaPagada()) {
                throw new ReglaNegocioException("Esta valorización ya se encuentra pagada.");
            }
            if (montoEscala.compareTo(val.getMontoValorizado().setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new ReglaNegocioException(
                        "El monto debe coincidir con la valorización (S/ " + val.getMontoValorizado() + ").");
            }
            conceptoIngreso = "Pago de Valorización N° " + val.getNumero() + " (" + codigoOp + ")";
        }

        Pago guardado = persistirPagoAprobado(pagoPort, ingresoPort, comprobantePort, valorizacionPort, clock,
                obra, val, comp, montoEscala, metodo, codigoOp,
                "COBRO_INTERNO", null, null, montoEscala, "PEN", null, null, null, conceptoIngreso);
        if (auditoriaPort != null) {
            auditoriaPort.registrar(AuditoriaEvento.crear(
                    clock, actor, "PAGO_INTERNO_APROBADO", "pago", String.valueOf(guardado.getIdPago()),
                    "EXITO", "Obra " + idObra + " monto S/ " + montoEscala + " op " + codigoOp));
        }
        return guardado;
    }

    @Override
    public DetalleEnlacePublico consultarEnlacePublico(String codigoPago) {
        Comprobante comp = comprobantePort.findByCodigoPago(codigoPago == null ? "" : codigoPago.trim())
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El enlace de pago no existe o ya no está disponible."));

        Valorizacion val = valorizacionPort.findById(comp.getIdValorizacion()).orElse(null);
        Obra obra = val != null ? obraPort.findById(val.getIdObra()).orElse(null) : null;
        Cliente cliente = obra != null && obra.getIdCliente() != null && clientePort != null
                ? clientePort.findById(obra.getIdCliente()).orElse(null) : null;

        BigDecimal tc = obtenerTipoCambioUsdPen(tipoCambioPort);
        BigDecimal montoUsd = comp.getTotal().divide(tc, 2, RoundingMode.HALF_UP);
        LocalDateTime ahora = LocalDateTime.now(clock);
        boolean pagado = "PAGADO".equalsIgnoreCase(comp.getEstado());
        boolean expirado = !pagado && !comp.esEnlaceVigente(ahora);
        boolean pasarelaHabilitada = pasarelaExternaPort != null && pasarelaExternaPort.estaHabilitada();

        ControlIntentosPago.Estado intentos = new ControlIntentosPago.Estado(
                false, null, ControlIntentosPago.MAX_INTENTOS_RECHAZADOS);
        if (pagoPort != null && !pagado && !expirado) {
            intentos = estadoIntentos(pagoPort, comp, horasVigenciaEnlace, ahora);
        }

        return new DetalleEnlacePublico(
                comp.getSerieNumero(),
                comp.getFechaEmision() != null ? comp.getFechaEmision().toString() : "-",
                comp.getSubtotal(),
                comp.getIgv(),
                comp.getTotal(),
                montoUsd,
                tc,
                comp.getEstado(),
                comp.getVenceEl(),
                expirado,
                pagado,
                pasarelaHabilitada,
                obra != null ? obra.getNombre() : "-",
                val != null ? val.getNumero() : 1,
                val != null ? val.getPeriodo() : "-",
                val != null ? val.getPorcentajeAvancePeriodo() : BigDecimal.ZERO,
                cliente != null ? cliente.getNombreRazonSocial()
                        : (obra != null && obra.getCliente() != null ? obra.getCliente() : "-"),
                intentos.bloqueado(),
                intentos.bloqueadoHasta(),
                intentos.intentosRestantes()
        );
    }

    private String resolverCodigoOperacion(String codigoOperacion, String prefijo) {
        if (codigoOperacion == null || codigoOperacion.isBlank()) {
            return prefijo + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        String norm = codigoOperacion.trim().toUpperCase();
        if (pagoPort.findByCodigoOperacion(norm).isPresent()) {
            throw new ReglaNegocioException("El código de operación " + norm + " ya fue registrado.");
        }
        return norm;
    }

    // ---------- Utilidades de dominio compartidas con CU-20 y CU-24 ----------

    record ContextoEnlace(Comprobante comprobante, Valorizacion valorizacion, Obra obra) {
    }

    static ContextoEnlace validarEnlacePagable(ComprobantePort comprobantePort, ValorizacionPort valorizacionPort,
                                               ObraPort obraPort, PagoPort pagoPort, Clock clock,
                                               int horasVigenciaEnlace, String codigoPago, boolean bloquear) {
        if (codigoPago == null || codigoPago.isBlank()) {
            throw new RecursoNoEncontradoException("Enlace de pago inválido.");
        }
        Comprobante comp = comprobantePort.findByCodigoPago(codigoPago.trim())
                .filter(Comprobante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El enlace de pago no existe o fue anulado."));
        if (bloquear) {
            comp = comprobantePort.findByIdParaActualizar(comp.getIdComprobante()).orElse(comp);
        }
        if (!comp.esPagable()) {
            throw new ReglaNegocioException("Este comprobante ya fue pagado o se encuentra en proceso de cobro.");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (!comp.esEnlaceVigente(ahora)) {
            throw new ReglaNegocioException("El enlace de pago ha expirado (vigencia 72h). Solicita su renovación al contador.");
        }
        ControlIntentosPago.Estado intentos = estadoIntentos(pagoPort, comp, horasVigenciaEnlace, ahora);
        if (intentos.bloqueado()) {
            throw new ReglaNegocioException("Superaste el límite de " + ControlIntentosPago.MAX_INTENTOS_RECHAZADOS
                    + " intentos de pago. Por seguridad el enlace está bloqueado. Vuelve a intentar en "
                    + intentos.minutosRestantes(ahora) + " minuto(s) o comunícate con la empresa.");
        }
        Valorizacion val = (bloquear
                ? valorizacionPort.findByIdParaActualizar(comp.getIdValorizacion())
                : valorizacionPort.findById(comp.getIdValorizacion()))
                .filter(Valorizacion::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("La valorización asociada no existe."));
        if (val.estaPagada()) {
            throw new ReglaNegocioException("La valorización ya fue pagada.");
        }
        Obra obra = obraPort.findById(val.getIdObra()).orElse(null);
        if (obra == null || !obra.isOperable()) {
            throw new ReglaNegocioException("La obra asociada se encuentra cerrada.");
        }
        return new ContextoEnlace(comp, val, obra);
    }

    static ControlIntentosPago.Estado estadoIntentos(PagoPort pagoPort, Comprobante comp,
                                                     int horasVigenciaEnlace, LocalDateTime ahora) {
        LocalDateTime desde = comp.getVenceEl() != null ? comp.getVenceEl().minusHours(horasVigenciaEnlace) : null;
        return ControlIntentosPago.evaluar(pagoPort.findFechasRechazosDesde(comp.getIdComprobante(), desde), ahora);
    }

    static String mensajeTrasRechazo(PagoPort pagoPort, Clock clock, Comprobante comp,
                                     int horasVigenciaEnlace, String motivo) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        ControlIntentosPago.Estado intentos = estadoIntentos(pagoPort, comp, horasVigenciaEnlace, ahora);
        String base = motivo == null || motivo.isBlank() ? "Pago rechazado." : motivo.trim();
        if (intentos.bloqueado()) {
            return base + " Llegaste al límite de " + ControlIntentosPago.MAX_INTENTOS_RECHAZADOS
                    + " intentos: el enlace queda bloqueado por " + ControlIntentosPago.BLOQUEO.toMinutes() + " minutos.";
        }
        return base + " Te quedan " + intentos.intentosRestantes() + " intento(s).";
    }

    static BigDecimal obtenerTipoCambioUsdPen(TipoCambioPort tipoCambioPort) {
        if (tipoCambioPort == null) {
            return GestionarTipoCambio.TIPO_CAMBIO_DEFECTO;
        }
        return tipoCambioPort.findByPar("USD", "PEN")
                .map(TipoCambio::getValor)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .orElse(GestionarTipoCambio.TIPO_CAMBIO_DEFECTO);
    }

    static void registrarIntentoRechazado(PagoPort pagoPort, Clock clock, Integer idObra, Integer idValorizacion,
                                          Integer idComprobante, BigDecimal monto, String metodoPago, String pasarela,
                                          String idOrdenExterna, String idCapturaExterna,
                                          BigDecimal montoCobro, String monedaCobro, BigDecimal tc,
                                          String marca, String ultimos4, String motivo) {
        Pago intento = new Pago();
        intento.setIdObra(idObra);
        intento.setIdValorizacion(idValorizacion);
        intento.setIdComprobante(idComprobante);
        intento.setMonto(monto);
        intento.setMetodoPago(metodoPago);
        intento.setCodigoOperacion("REJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        intento.setPasarela(pasarela);
        intento.setIdOrdenExterna(idOrdenExterna);
        intento.setIdCapturaExterna(idCapturaExterna);
        intento.setMontoCobro(montoCobro);
        intento.setMonedaCobro(monedaCobro);
        intento.setTipoCambio(tc);
        intento.setMarcaTarjeta(marca);
        intento.setUltimos4(ultimos4);
        intento.setMotivo(motivo != null && motivo.length() > 250 ? motivo.substring(0, 250) : motivo);
        intento.setFechaPago(LocalDateTime.now(clock));
        intento.setEstado("RECHAZADO");
        intento.setActivo(true);
        pagoPort.guardar(intento);
    }

    static Pago persistirPagoAprobado(PagoPort pagoPort, IngresoPort ingresoPort, ComprobantePort comprobantePort,
                                      ValorizacionPort valorizacionPort, Clock clock, Obra obra, Valorizacion val,
                                      Comprobante comp, BigDecimal montoPen, String metodo, String codigoOp,
                                      String pasarela, String idOrdenExterna, String idCapturaExterna,
                                      BigDecimal montoCobro, String monedaCobro, BigDecimal tc,
                                      String marca, String ultimos4, String conceptoIngreso) {
        Pago pago = new Pago();
        pago.setIdObra(obra.getIdObra());
        pago.setIdValorizacion(val != null ? val.getIdValorizacion() : null);
        pago.setIdComprobante(comp != null ? comp.getIdComprobante() : null);
        pago.setMonto(montoPen);
        pago.setMetodoPago(metodo);
        pago.setCodigoOperacion(codigoOp);
        pago.setPasarela(pasarela);
        pago.setIdOrdenExterna(idOrdenExterna);
        pago.setIdCapturaExterna(idCapturaExterna);
        pago.setMontoCobro(montoCobro);
        pago.setMonedaCobro(monedaCobro);
        pago.setTipoCambio(tc);
        pago.setMarcaTarjeta(marca);
        pago.setUltimos4(ultimos4);
        pago.setMotivo("Aprobado");
        pago.setFechaPago(LocalDateTime.now(clock));
        pago.setEstado("APROBADO");
        pago.setActivo(true);
        Pago pagoGuardado = pagoPort.guardar(pago);

        Ingreso ing = new Ingreso();
        ing.setIdObra(obra.getIdObra());
        ing.setIdPago(pagoGuardado.getIdPago());
        ing.setFecha(LocalDate.now(clock));
        ing.setConcepto(conceptoIngreso.length() > 150 ? conceptoIngreso.substring(0, 150) : conceptoIngreso);
        ing.setMonto(montoPen);
        ing.setActivo(true);
        Ingreso ingresoGuardado = ingresoPort.guardar(ing);

        pagoGuardado.setIdIngresoGenerado(ingresoGuardado.getIdIngreso());
        pagoGuardado = pagoPort.guardar(pagoGuardado);

        if (comp != null) {
            comp.setEstado("PAGADO");
            comprobantePort.guardar(comp);
        }
        if (val != null) {
            val.setEstado("PAGADA");
            valorizacionPort.guardar(val);
        }
        return pagoGuardado;
    }
}
