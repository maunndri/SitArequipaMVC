package dao.jdbc;

import dao.ViajeDAO;
import excepcion.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.MotivoRechazo;
import modelo.Viaje;

public class ViajeDAOJdbc implements ViajeDAO {
    @Override
    public int insertar(Viaje v) {
        String sql = "INSERT INTO viaje (numero_tarjeta, id_lector, id_ruta, id_estado_viaje, tarifa) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getNumeroTarjeta());
            ps.setString(2, v.getIdLector());
            ps.setInt(3, v.getIdRuta());
            ps.setInt(4, v.getEstado().getId());
            ps.setBigDecimal(5, v.getTarifa());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se generó id_viaje");
                return rs.getInt("id_viaje");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar viaje", e);
        }
    }

    @Override
    public void insertarRechazo(int idViaje, MotivoRechazo motivo) {
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(
                 "INSERT INTO registro_rechazo (id_viaje, id_motivo_rechazo) VALUES (?, ?)")) {
            ps.setInt(1, idViaje);
            ps.setInt(2, motivo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar rechazo", e);
        }
    }
}
