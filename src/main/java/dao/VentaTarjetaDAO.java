package dao;

import java.math.BigDecimal;

public interface VentaTarjetaDAO {
    void insertar(String numeroTarjeta, int idOperador, String numeroComprobante, BigDecimal montoPagado);
}
