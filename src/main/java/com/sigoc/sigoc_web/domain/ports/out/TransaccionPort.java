package com.sigoc.sigoc_web.domain.ports.out;

import java.util.function.Supplier;

/**
 * Puerto de salida para demarcar transacciones cortas independientes en flujos
 * que invocan llamadas HTTP externas (evita retener locks pesimistas durante I/O).
 */
public interface TransaccionPort {

    <T> T ejecutarNueva(Supplier<T> accion);

    <T> T ejecutarNuevaSinRollbackPagoRechazado(Supplier<T> accion);

    static TransaccionPort directo() {
        return new TransaccionPort() {
            @Override
            public <T> T ejecutarNueva(Supplier<T> accion) {
                return accion.get();
            }

            @Override
            public <T> T ejecutarNuevaSinRollbackPagoRechazado(Supplier<T> accion) {
                return accion.get();
            }
        };
    }
}
