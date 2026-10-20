package dao;

import java.util.Optional;
import modelo.Usuario;

public interface UsuarioDAO {
    Optional<Usuario> buscarPorUsername(String username);
}
