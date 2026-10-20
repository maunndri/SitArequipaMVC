package excepcion;

/** Error técnico de base de datos. NUNCA se muestra al usuario: solo se registra en el log. */
public class PersistenciaException extends SitException {
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
