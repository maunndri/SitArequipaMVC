package util;

import excepcion.AutenticacionException;
import excepcion.NegocioException;
import excepcion.ValidacionException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Convierte excepciones en mensajes seguros: el detalle técnico solo va al log. */
public final class ManejadorErrores {
    private static final Logger LOG = Logger.getLogger(ManejadorErrores.class.getName());
    private ManejadorErrores() {}

    public static String mensajeSeguro(Throwable e) {
        if (e instanceof ValidacionException || e instanceof NegocioException || e instanceof AutenticacionException) {
            return e.getMessage();
        }
        String incidente = UUID.randomUUID().toString().substring(0, 8);
        LOG.log(Level.SEVERE, "Error inesperado [incidente " + incidente + "]", e);
        return "Ocurrió un error inesperado. Si persiste, informe el código de incidente " + incidente + ".";
    }
}
