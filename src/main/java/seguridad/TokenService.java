package seguridad;

import modelo.SesionUsuario;

public interface TokenService {
    String emitir(SesionUsuario base);
    /** @throws excepcion.AutenticacionException si el token es inválido o expiró */
    SesionUsuario validar(String token);
    long ttlSegundos();
}
