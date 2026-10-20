package dao.jdbc;

import dao.TarjetaDAO;
import excepcion.PersistenciaException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import modelo.EstadoTarjeta;
import modelo.PerfilTarifa;
import modelo.Tarjeta;

public class TarjetaDAOJdbc implements TarjetaDAO {
    private static final String SELECT = "SELECT numero_tarjeta, id_pasajero, id_perfil_tarifa, "
        + "id_estado_tarjeta, saldo, fecha_emision FROM tarjeta WHERE numero_tarjeta = ?";

    @Override
    public Optional<Tarjeta> buscarPorNumero(String numero) { return consultar(SELECT, numero); }

    @Override
    public Optional<Tarjeta> buscarParaActualizar(String numero) { return consultar(SELECT + " FOR UPDATE", numero); }

    private Optional<Tarjeta> consultar(String sql, String numero) {
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Tarjeta(rs.getString("numero_tarjeta"), rs.getInt("id_pasajero"),
                    PerfilTarifa.desdeId(rs.getInt("id_perfil_tarifa")),
                    EstadoTarjeta.desdeId(rs.getInt("id_estado_tarjeta")),
                    rs.getBigDecimal("saldo"), rs.getDate("fecha_emision").toLocalDate()));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar tarjeta", e);
        }
    }

    @Override
    public boolean existeActivaDePasajero(int idPasajero) {
        String sql = "SELECT 1 FROM tarjeta WHERE id_pasajero = ? AND id_estado_tarjeta = ?";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setInt(1, idPasajero);
            ps.setInt(2, EstadoTarjeta.ACTIVA.getId());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar tarjeta activa", e);
        }
    }

    @Override
    public void insertar(Tarjeta t) {
        String sql = "INSERT INTO tarjeta (numero_tarjeta, id_pasajero, id_perfil_tarifa, id_estado_tarjeta, saldo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, t.getNumero());
            ps.setInt(2, t.getIdPasajero());
            ps.setInt(3, t.getPerfil().getId());
            ps.setInt(4, t.getEstado().getId());
            ps.setBigDecimal(5, t.getSaldo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar tarjeta", e);
        }
    }

    @Override
    public void actualizarSaldo(String numero, BigDecimal nuevoSaldo) {
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(
                 "UPDATE tarjeta SET saldo = ? WHERE numero_tarjeta = ?")) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setString(2, numero);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar saldo", e);
        }
    }
}
