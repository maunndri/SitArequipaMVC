package dao;

import java.math.BigDecimal;

public interface ComprobanteDAO {
    void insertar(String numeroComprobante, String tipo, BigDecimal monto);
}
