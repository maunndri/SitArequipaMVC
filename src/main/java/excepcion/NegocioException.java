package excepcion;

/** Violación de una regla de negocio (saldo insuficiente, tarjeta bloqueada...). Mensaje seguro. */
public class NegocioException extends SitException {
    public NegocioException(String mensaje) { super(mensaje); }
}
