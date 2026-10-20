package dao.jdbc;

import dao.VentaTarjetaDAO;
import excepcion.PersistenciaException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VentaTarjetaDAOJdbc implements VentaTarjetaDAO {
    @Override
    public void insertar(String numeroTarjeta, int idOperador, String numeroComprobante, BigDecimal montoPagado) {
        String sql = "INSERT INTO venta_tarjeta (numero_tarjeta, id_operador, numero_comprobante, monto_pagado) "
                   + "VALUES (?, ?, ?, ?)";
        try (ConexionBD.Sesion s = ConexionBD.abrir();
             PreparedStatement ps = s.conexion().prepareStatement(sql)) {
            ps.setString(1, numeroTarjeta);
            ps.setInt(2, idOperador);
            ps.setString(3, numeroComprobante);
            ps.setBigDecimal(4, montoPagado);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar venta", e);
        }
    }
}
