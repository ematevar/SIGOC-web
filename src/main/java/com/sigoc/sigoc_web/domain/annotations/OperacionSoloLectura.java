package com.sigoc.sigoc_web.domain.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca explícita de dominio para operaciones de solo lectura en puertos de entrada.
 * Permite que el proxy transaccional de infraestructura abra transacciones readOnly=true
 * sin depender de convenciones frágiles de nombres de método (ADR-09).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperacionSoloLectura {
}
