package dao.jdbc;

import dao.UsuarioDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import modelo.Rol;
import modelo.Usuario;

public class UsuarioDAOJdbc implements UsuarioDAO {

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql = "SELECT u.id_usuario, u.id_persona, u.username, u.password_hash, u.activo, r.nombre_rol "
                   + "FROM usuario u JOIN rol r ON r.id_rol = u.id_rol WHERE u.username = ?";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Usuario(rs.getInt("id_usuario"), rs.getInt("id_persona"),
                    rs.getString("username"), rs.getString("password_hash"),
                    Rol.valueOf(rs.getString("nombre_rol")), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar usuario", e);
        }
    }
}
