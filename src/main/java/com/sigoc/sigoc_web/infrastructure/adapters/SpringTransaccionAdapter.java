package com.sigoc.sigoc_web.infrastructure.adapters;

import com.sigoc.sigoc_web.domain.exceptions.PagoRechazadoException;
import com.sigoc.sigoc_web.domain.ports.out.TransaccionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

/**
 * Adaptador de infraestructura que ejecuta bloques en transacciones independientes
 * (PROPAGATION_REQUIRES_NEW) para liberar bloqueos de BD antes de llamadas HTTP externas.
 */
@Component
public class SpringTransaccionAdapter implements TransaccionPort {

    private final PlatformTransactionManager txManager;

    public SpringTransaccionAdapter(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }

    @Override
    public <T> T ejecutarNueva(Supplier<T> accion) {
        return ejecutarInterno(accion, false);
    }

    @Override
    public <T> T ejecutarNuevaSinRollbackPagoRechazado(Supplier<T> accion) {
        return ejecutarInterno(accion, true);
    }

    private <T> T ejecutarInterno(Supplier<T> accion, boolean conservarEnPagoRechazado) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            T resultado = accion.get();
            txManager.commit(status);
            return resultado;
        } catch (PagoRechazadoException ex) {
            if (conservarEnPagoRechazado) {
                txManager.commit(status);
            } else if (!status.isCompleted()) {
                txManager.rollback(status);
            }
            throw ex;
        } catch (Throwable ex) {
            if (!status.isCompleted()) {
                txManager.rollback(status);
            }
            throw ex;
        }
    }
}
