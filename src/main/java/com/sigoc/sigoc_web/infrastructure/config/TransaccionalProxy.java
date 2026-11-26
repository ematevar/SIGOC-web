package com.sigoc.sigoc_web.infrastructure.config;

import com.sigoc.sigoc_web.domain.annotations.OperacionSoloLectura;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Decorador transaccional en la frontera de cableado de infraestructura.
 * Permite que los casos de uso de aplicación permanezcan libres de Spring/Jakarta
 * y que los controladores web no declaren @Transactional.
 * Toda operación es lectura-escritura por defecto salvo que el método del puerto
 * declare explícitamente {@link OperacionSoloLectura}.
 */
public final class TransaccionalProxy {

    private TransaccionalProxy() {
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T envolver(Class<T> interfazPuerto, T implementacion,
                                 PlatformTransactionManager txManager,
                                 Class<? extends Throwable>... sinRollbackPara) {
        return (T) Proxy.newProxyInstance(
                interfazPuerto.getClassLoader(),
                new Class<?>[]{interfazPuerto},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(implementacion, args);
                    }
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    def.setReadOnly(esSoloLectura(method));
                    TransactionStatus status = txManager.getTransaction(def);
                    try {
                        Object resultado = method.invoke(implementacion, args);
                        txManager.commit(status);
                        return resultado;
                    } catch (InvocationTargetException ex) {
                        Throwable causa = ex.getTargetException();
                        if (debeHacerCommit(causa, sinRollbackPara)) {
                            txManager.commit(status);
                        } else if (!status.isCompleted()) {
                            txManager.rollback(status);
                        }
                        throw causa;
                    } catch (Throwable ex) {
                        if (!status.isCompleted()) {
                            txManager.rollback(status);
                        }
                        throw ex;
                    }
                }
        );
    }

    static boolean esSoloLectura(Method method) {
        return method.isAnnotationPresent(OperacionSoloLectura.class);
    }

    private static boolean debeHacerCommit(Throwable causa, Class<? extends Throwable>[] sinRollbackPara) {
        if (sinRollbackPara == null) {
            return false;
        }
        for (Class<? extends Throwable> tipo : sinRollbackPara) {
            if (tipo.isInstance(causa)) {
                return true;
            }
        }
        return false;
    }
}
