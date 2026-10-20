package dao;

import java.util.Optional;
import modelo.Pasajero;

public interface PasajeroDAO {
    Optional<Pasajero> buscarPorDocumento(String documento);
    /** Inserta persona (+ teléfono) y pasajero. Devuelve el pasajero con su id asignado. */
    Pasajero insertar(Pasajero pasajero);
}
