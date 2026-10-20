package dao;

import java.util.function.Supplier;

/** Unidad de trabajo ACID (RNF-002). Los servicios no conocen JDBC. */
public interface Transaccion {
    <T> T ejecutar(Supplier<T> trabajo);

    default void ejecutar(Runnable trabajo) {
        ejecutar(() -> { trabajo.run(); return null; });
    }
}
