package util;

import excepcion.AutenticacionException;
import excepcion.NegocioException;
import excepcion.ValidacionException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convierte excepciones en mensajes seguros: el detalle técnico solo va al log (Sección 2.7.4 y 2.8.2).
 * Principio de no revelación de errores técnicos al usuario final.
 */
public final class ManejadorErrores {
    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);
    private ManejadorErrores() {}

    public static String mensajeSeguro(Throwable e) {
        if (e instanceof ValidacionException || e instanceof NegocioException || e instanceof AutenticacionException) {
            return e.getMessage();
        }
        String incidente = UUID.randomUUID().toString().substring(0, 8);
        LOG.error("Error inesperado [incidente {}]", incidente, e);
        return "Ocurrió un error inesperado. Si persiste, informe el código de incidente " + incidente + ".";
    }
}
