package dao.jdbc;

import dao.RecargaDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelo.Recarga;

public class RecargaDAOJdbc implements RecargaDAO {
    @Override
    public void insertar(Recarga r) {
        String sql = "INSERT INTO recarga (numero_tarjeta, id_operador, numero_comprobante, monto, "
                   + "saldo_anterior, saldo_posterior) VALUES (?, ?, ?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, r.getNumeroTarjeta());
            ps.setInt(2, r.getIdOperador());
            ps.setString(3, r.getNumeroComprobante());
            ps.setBigDecimal(4, r.getMonto());
            ps.setBigDecimal(5, r.getSaldoAnterior());
            ps.setBigDecimal(6, r.getSaldoPosterior());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar recarga", e);
        }
    }
}
