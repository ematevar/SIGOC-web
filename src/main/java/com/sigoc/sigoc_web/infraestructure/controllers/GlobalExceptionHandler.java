package com.sigoc.sigoc_web.infraestructure.controllers;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.RecursoNoEncontradoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.infraestructure.controllers.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Traduce excepciones de dominio y técnicas a respuestas HTTP con mensajes claros
 * para el usuario final (equivalente a ClsConstantes.mensajeUsuario, RNF-08).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException ex) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos inválidos." : mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> formato(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Uno de los valores ingresados tiene un formato inválido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        String raw = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        String mensaje;
        if (raw.contains("duplicate entry") || raw.contains("unique")) {
            if (raw.contains("ruc")) {
                mensaje = "Ya existe un proveedor registrado con ese número de RUC.";
            } else if (raw.contains("dni")) {
                mensaje = "Ya existe un trabajador registrado con ese número de DNI.";
            } else if (raw.contains("usuario_login")) {
                mensaje = "El nombre de usuario ya está en uso. Elige otro.";
            } else if (raw.contains("comprobante")) {
                mensaje = "Ya se registró ese número de comprobante para el proveedor seleccionado.";
            } else {
                mensaje = "El registro ya existe (valor duplicado). Verifica los datos ingresados.";
            }
            return respuesta(HttpStatus.CONFLICT, mensaje);
        }
        if (raw.contains("foreign key") || raw.contains("constraint fails")) {
            return respuesta(HttpStatus.CONFLICT,
                    "No se puede completar la operación porque el registro está relacionado con otros movimientos del sistema.");
        }
        return respuesta(HttpStatus.BAD_REQUEST,
                "Uno de los valores ingresados excede el límite permitido o tiene un formato inválido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generico(Exception ex) {
        log.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo completar la operación solicitada. Se registró el detalle técnico en el log del sistema.");
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), mensaje));
    }
}
