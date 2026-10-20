package dao.jdbc;

import excepcion.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Acceso a conexiones. Si hay una transacción activa en el hilo, los DAO reutilizan
 * esa misma conexión (y no la cierran); si no, abren una nueva por operación.
 * Credenciales: variables de entorno SIT_DB_URL / SIT_DB_USER / SIT_DB_PASSWORD tienen
 * prioridad sobre db.properties (así no se versionan secretos).
 */
public final class ConexionBD {
    private static final ThreadLocal<Connection> ACTUAL = new ThreadLocal<>();
    private static volatile Properties config;

    private ConexionBD() {}

    /** Recurso de un DAO: cierra la conexión solo si es suya (no si pertenece a una transacción). */
    public static final class Sesion implements AutoCloseable {
        private final Connection conexion;
        private final boolean propia;

        private Sesion(Connection conexion, boolean propia) { this.conexion = conexion; this.propia = propia; }
        public Connection conexion() { return conexion; }

        @Override
        public void close() throws SQLException {
            if (propia) conexion.close();
        }
    }

    public static Sesion abrir() throws SQLException {
        Connection c = ACTUAL.get();
        return c != null ? new Sesion(c, false) : new Sesion(nuevaConexion(), true);
    }

    static Connection nuevaConexion() throws SQLException {
        Properties p = cargar();
        return DriverManager.getConnection(
            valor("SIT_DB_URL", p, "jdbc.url"), valor("SIT_DB_USER", p, "jdbc.user"),
            valor("SIT_DB_PASSWORD", p, "jdbc.password"));
    }

    static boolean hayTransaccion() { return ACTUAL.get() != null; }
    static void vincular(Connection c) { ACTUAL.set(c); }
    static void desvincular() { ACTUAL.remove(); }

    private static String valor(String env, Properties p, String clave) {
        String e = System.getenv(env);
        return (e != null && !e.isEmpty()) ? e : p.getProperty(clave, "");
    }

    private static Properties cargar() {
        if (config == null) {
            synchronized (ConexionBD.class) {
                if (config == null) {
                    Properties p = new Properties();
                    try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
                        if (in != null) p.load(in);
                        Class.forName("org.postgresql.Driver");
                    } catch (IOException | ClassNotFoundException e) {
                        throw new PersistenciaException("No se pudo inicializar la configuración de BD", e);
                    }
                    config = p;
                }
            }
        }
        return config;
    }
}
