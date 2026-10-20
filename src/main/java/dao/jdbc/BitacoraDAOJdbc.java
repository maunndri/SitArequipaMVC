package dao.jdbc;

import dao.BitacoraDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BitacoraDAOJdbc implements BitacoraDAO {
    @Override
    public void registrar(Integer idUsuario, String accion, String entidad, String ip) {
        String sql = "INSERT INTO bitacora (id_usuario, accion, entidad, ip) VALUES (?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            if (idUsuario == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, entidad);
            ps.setString(4, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al escribir bitácora", e);
        }
    }
}
