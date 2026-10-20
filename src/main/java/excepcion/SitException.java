package excepcion;

/** Raíz de las excepciones del sistema (no verificadas). */
public class SitException extends RuntimeException {
    public SitException(String mensaje) { super(mensaje); }
    public SitException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
