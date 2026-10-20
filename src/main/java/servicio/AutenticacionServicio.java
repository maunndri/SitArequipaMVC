package servicio;

import dao.BitacoraDAO;
import dao.UsuarioDAO;
import excepcion.AutenticacionException;
import excepcion.PersistenciaException;
import excepcion.ValidacionException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.SesionUsuario;
import modelo.Usuario;
import seguridad.LimitadorIntentos;
import seguridad.PasswordHasher;
import seguridad.TokenService;
import util.Validador;

public class AutenticacionServicio {
    private static final Logger LOG = Logger.getLogger(AutenticacionServicio.class.getName());
    private static final String MSG_GENERICO = "Usuario o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO;
    private final PasswordHasher hasher;
    private final TokenService tokens;
    private final BitacoraDAO bitacora;
    private final LimitadorIntentos limitador;
    private final String hashFicticio;

    public AutenticacionServicio(UsuarioDAO usuarioDAO, PasswordHasher hasher, TokenService tokens,
                                 BitacoraDAO bitacora, LimitadorIntentos limitador) {
        this.usuarioDAO = usuarioDAO; this.hasher = hasher; this.tokens = tokens;
        this.bitacora = bitacora; this.limitador = limitador;
        this.hashFicticio = hasher.hash("contraseña-ficticia");   // iguala el tiempo de respuesta si el usuario no existe
    }

    /** @return JWT firmado */
    public String iniciarSesion(String username, String password, String ip) {
        String clave;
        try {
            clave = Validador.username(username).toLowerCase() + "|" + ip;
        } catch (ValidacionException e) {
            throw new AutenticacionException(MSG_GENERICO);       // no revelamos qué falló
        }
        if (password == null || password.isEmpty() || password.length() > 100) throw new AutenticacionException(MSG_GENERICO);
        if (limitador.bloqueado(clave)) throw new AutenticacionException("Demasiados intentos. Intente de nuevo más tarde.");

        Optional<Usuario> encontrado = usuarioDAO.buscarPorUsername(username.trim());
        boolean passwordOk = hasher.verificar(password, encontrado.map(Usuario::getPasswordHash).orElse(hashFicticio));

        if (encontrado.isEmpty() || !encontrado.get().isActivo() || !passwordOk) {
            limitador.registrarFallo(clave);
            auditar(null, "LOGIN_FALLIDO", "usuario", ip);
            throw new AutenticacionException(MSG_GENERICO);
        }

        Usuario u = encontrado.get();
        limitador.limpiar(clave);
        auditar(u.getId(), "LOGIN_OK", "usuario", ip);
        return tokens.emitir(new SesionUsuario(u.getId(), u.getIdPersona(), u.getUsername(), u.getRol(), 0));
    }

    private void auditar(Integer idUsuario, String accion, String entidad, String ip) {
        try {
            bitacora.registrar(idUsuario, accion, entidad, ip);
        } catch (PersistenciaException e) {
            LOG.log(Level.SEVERE, "No se pudo escribir la bitácora", e);
        }
    }
}
