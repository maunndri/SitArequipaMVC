package excepcion;

/** Entrada del usuario inválida. El mensaje es seguro de mostrar. */
public class ValidacionException extends SitException {
    public ValidacionException(String mensaje) { super(mensaje); }
}
