package com.sigoc.sigoc_web.infrastructure.config;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Decorador transaccional en la frontera de cableado de infraestructura.
 * Permite que los casos de uso de aplicación permanezcan libres de Spring/Jakarta
 * y que los controladores web no declaren @Transactional.
 */
public final class TransaccionalProxy {

    private static final Set<String> PREFIJOS_LECTURA = Set.of(
            "listar", "obtener", "consultar", "previsualizar", "generar", "exportar", "buscar", "ejecutar"
    );

    private TransaccionalProxy() {
    }

    public static <T> T envolver(Class<T> interfazPuerto, T implementacion,
                                 PlatformTransactionManager txManager,
                                 Class<? extends Throwable>... sinRollbackPara) {
        return envolver(interfazPuerto, implementacion, txManager, false, sinRollbackPara);
    }

    @SuppressWarnings("unchecked")
    public static <T> T envolver(Class<T> interfazPuerto, T implementacion,
                                 PlatformTransactionManager txManager,
                                 boolean forzarLecturaEscrituraEnEjecutar,
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
                    def.setReadOnly(esSoloLectura(method, forzarLecturaEscrituraEnEjecutar));
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

    private static boolean esSoloLectura(Method method, boolean forzarLecturaEscrituraEnEjecutar) {
        String nombre = method.getName();
        if (forzarLecturaEscrituraEnEjecutar && "ejecutar".equals(nombre)) {
            return false;
        }
        return PREFIJOS_LECTURA.stream().anyMatch(nombre::startsWith);
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
