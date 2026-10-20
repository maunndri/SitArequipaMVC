package dao.jdbc;

import dao.ComprobanteDAO;
import excepcion.PersistenciaException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ComprobanteDAOJdbc implements ComprobanteDAO {
    @Override
    public void insertar(String numeroComprobante, String tipo, BigDecimal monto) {
        String sql = "INSERT INTO comprobante (numero_comprobante, id_tipo_comprobante, monto) "
                   + "VALUES (?, (SELECT id_tipo_comprobante FROM tipo_comprobante WHERE nombre_tipo = ?), ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, numeroComprobante);
            ps.setString(2, tipo);
            ps.setBigDecimal(3, monto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar comprobante", e);
        }
    }
}
