package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.OrdenPayPal;
import com.sigoc.sigoc_web.domain.ports.out.PasarelaPagoPort.ResultadoCapturaPayPal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Integración con PayPal REST v2 Sandbox usando credenciales externalizadas (RF-20 / C01).
 */
@Component
public class PayPalSandboxAdapter {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public PayPalSandboxAdapter(
            @Value("${sigoc.paypal.base-url:https://api-m.sandbox.paypal.com}") String baseUrl,
            @Value("${sigoc.paypal.client-id:}") String clientId,
            @Value("${sigoc.paypal.client-secret:}") String clientSecret,
            @Value("${sigoc.paypal.timeout-seconds:10}") int timeoutSeconds) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.clientSecret = clientSecret == null ? "" : clientSecret.trim();
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofSeconds(Math.max(3, timeoutSeconds)));
        rf.setReadTimeout(Duration.ofSeconds(Math.max(3, timeoutSeconds)));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(rf)
                .build();
    }

    public boolean habilitado() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public OrdenPayPal crearOrden(BigDecimal montoUsd, String moneda, String referenciaComprobante,
                                  String descripcion, String returnUrl, String cancelUrl) {
        String token = obtenerTokenOAuth();
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(Map.of(
                        "reference_id", referenciaComprobante,
                        "description", descripcion,
                        "amount", Map.of(
                                "currency_code", moneda,
                                "value", montoUsd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                        )
                )),
                "application_context", Map.of(
                        "brand_name", "SIGOC Web",
                        "user_action", "PAY_NOW",
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl
                )
        );
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.post()
                    .uri("/v2/checkout/orders")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (resp == null || resp.get("id") == null) {
                throw new ReglaNegocioException("PayPal Sandbox no devolvió un identificador de orden.");
            }
            String id = resp.get("id").toString();
            String status = String.valueOf(resp.getOrDefault("status", "CREATED"));
            String approvalUrl = extraerLinkAprobacion(resp.get("links"));
            return new OrdenPayPal(id, status, approvalUrl);
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("Error al crear orden en PayPal Sandbox: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public ResultadoCapturaPayPal capturarOrden(String idOrdenExterna) {
        if (idOrdenExterna == null || idOrdenExterna.isBlank()) {
            throw new ReglaNegocioException("Falta el identificador de orden de PayPal.");
        }
        String token = obtenerTokenOAuth();
        try {
            Map<String, Object> resp = restClient.post()
                    .uri("/v2/checkout/orders/{id}/capture", idOrdenExterna.trim())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .body(Map.class);
            if (resp == null) {
                return new ResultadoCapturaPayPal(false, null, null, null, "Respuesta vacía de PayPal.");
            }
            String status = String.valueOf(resp.getOrDefault("status", ""));
            boolean ok = "COMPLETED".equalsIgnoreCase(status);
            String idCaptura = idOrdenExterna.trim();
            BigDecimal monto = null;
            String moneda = "USD";

            Object puObj = resp.get("purchase_units");
            if (puObj instanceof List<?> pus && !pus.isEmpty() && pus.get(0) instanceof Map<?, ?> pu) {
                Object payObj = pu.get("payments");
                if (payObj instanceof Map<?, ?> payments && payments.get("captures") instanceof List<?> caps && !caps.isEmpty()) {
                    Object cap0 = caps.get(0);
                    if (cap0 instanceof Map<?, ?> cap) {
                        if (cap.get("id") != null) {
                            idCaptura = cap.get("id").toString();
                        }
                        if (cap.get("amount") instanceof Map<?, ?> amount) {
                            if (amount.get("value") != null) {
                                monto = new BigDecimal(amount.get("value").toString());
                            }
                            if (amount.get("currency_code") != null) {
                                moneda = amount.get("currency_code").toString();
                            }
                        }
                    }
                }
            }
            return new ResultadoCapturaPayPal(ok, idCaptura, monto, moneda,
                    ok ? "COMPLETED" : "Estado PayPal: " + status);
        } catch (RestClientException ex) {
            return new ResultadoCapturaPayPal(false, null, null, null,
                    "Error al capturar orden PayPal Sandbox: " + ex.getMessage());
        }
    }

    private String obtenerTokenOAuth() {
        if (!habilitado()) {
            throw new ReglaNegocioException(
                    "PayPal Sandbox no tiene credenciales configuradas (PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET).");
        }
        String basic = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.post()
                    .uri("/v1/oauth2/token")
                    .header("Authorization", "Basic " + basic)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(Map.class);
            if (resp == null || resp.get("access_token") == null) {
                throw new ReglaNegocioException("No se pudo obtener token OAuth de PayPal Sandbox.");
            }
            return resp.get("access_token").toString();
        } catch (RestClientException ex) {
            throw new ReglaNegocioException("Error de autenticación con PayPal Sandbox: " + ex.getMessage());
        }
    }

    private String extraerLinkAprobacion(Object linksObj) {
        if (linksObj instanceof List<?> links) {
            for (Object item : links) {
                if (item instanceof Map<?, ?> link) {
                    String rel = String.valueOf(link.get("rel"));
                    if ("approve".equalsIgnoreCase(rel) || "payer-action".equalsIgnoreCase(rel)) {
                        return link.get("href") != null ? link.get("href").toString() : null;
                    }
                }
            }
        }
        return null;
    }
}
