package excepcion;

/** Credenciales o token inválidos. El mensaje es deliberadamente genérico. */
public class AutenticacionException extends SitException {
    public AutenticacionException(String mensaje) { super(mensaje); }
}
