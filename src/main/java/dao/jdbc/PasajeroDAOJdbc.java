package dao.jdbc;

import dao.PasajeroDAO;
import excepcion.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import modelo.Pasajero;

public class PasajeroDAOJdbc implements PasajeroDAO {

    @Override
    public Optional<Pasajero> buscarPorDocumento(String documento) {
        String sql = "SELECT p.id_persona, p.tipo_documento, p.documento_identidad, p.nombres, "
                   + "p.apellido_paterno, p.apellido_materno, p.email "
                   + "FROM persona p JOIN pasajero pa ON pa.id_persona = p.id_persona "
                   + "WHERE p.documento_identidad = ?";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Pasajero p = new Pasajero();
                p.setIdPersona(rs.getInt("id_persona"));
                p.setTipoDocumento(rs.getString("tipo_documento"));
                p.setDocumento(rs.getString("documento_identidad"));
                p.setNombres(rs.getString("nombres"));
                p.setApellidoPaterno(rs.getString("apellido_paterno"));
                p.setApellidoMaterno(rs.getString("apellido_materno"));
                p.setEmail(rs.getString("email"));
                return Optional.of(p);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar pasajero", e);
        }
    }

    @Override
    public Pasajero insertar(Pasajero p) {
        String sqlPersona = "INSERT INTO persona (nombres, apellido_paterno, apellido_materno, tipo_documento, "
                          + "documento_identidad, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir()) {
            Connection c = s.conexion();
            try (PreparedStatement ps = c.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNombres());
                ps.setString(2, p.getApellidoPaterno());
                ps.setString(3, p.getApellidoMaterno());
                ps.setString(4, p.getTipoDocumento());
                ps.setString(5, p.getDocumento());
                ps.setString(6, p.getEmail());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se generó id_persona");
                    p.setIdPersona(rs.getInt("id_persona"));
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO pasajero (id_persona) VALUES (?)")) {
                ps.setInt(1, p.getIdPersona());
                ps.executeUpdate();
            }
            if (p.getTelefono() != null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO persona_telefono (id_persona, telefono) VALUES (?, ?)")) {
                    ps.setInt(1, p.getIdPersona());
                    ps.setString(2, p.getTelefono());
                    ps.executeUpdate();
                }
            }
            return p;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar pasajero", e);
        }
    }
}
