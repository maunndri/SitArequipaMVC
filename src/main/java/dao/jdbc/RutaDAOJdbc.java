package dao.jdbc;

import dao.RutaDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import modelo.Ruta;

public class RutaDAOJdbc implements RutaDAO {
    @Override
    public Optional<Ruta> buscarPorId(int idRuta) {
        String sql = "SELECT id_ruta, codigo_ruta, nombre_ruta, tarifa_base, activo FROM ruta WHERE id_ruta = ?";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Ruta(rs.getInt("id_ruta"), rs.getString("codigo_ruta"),
                    rs.getString("nombre_ruta"), rs.getBigDecimal("tarifa_base"), rs.getBoolean("activo")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar ruta", e);
        }
    }
}
