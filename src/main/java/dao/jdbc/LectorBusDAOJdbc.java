package dao.jdbc;

import dao.LectorBusDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LectorBusDAOJdbc implements LectorBusDAO {
    @Override
    public boolean existe(String idLector) {
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement("SELECT 1 FROM lector_bus WHERE id_lector = ?")) {
            ps.setString(1, idLector);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar lector", e);
        }
    }
}
