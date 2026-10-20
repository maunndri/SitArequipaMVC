package dao.jdbc;

import dao.Transaccion;
import excepcion.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransaccionJdbc implements Transaccion {

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        if (ConexionBD.hayTransaccion()) return trabajo.get();      // transacción anidada: se une a la existente

        try (Connection c = ConexionBD.nuevaConexion()) {
            c.setAutoCommit(false);
            ConexionBD.vincular(c);
            try {
                T resultado = trabajo.get();
                c.commit();
                return resultado;
            } catch (RuntimeException | SQLException e) {
                try { c.rollback(); } catch (SQLException ignorada) { e.addSuppressed(ignorada); }
                if (e instanceof RuntimeException re) throw re;
                throw new PersistenciaException("Fallo al confirmar la transacción", e);
            } finally {
                ConexionBD.desvincular();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo abrir la transacción", e);
        }
    }
}
