package dao;

import java.util.List;
import java.util.Optional;
import modelo.Pasajero;

public interface PasajeroDAO {
    Optional<Pasajero> buscarPorDocumento(String documento);
    Optional<Pasajero> buscarPorId(int idPersona);
    /** Inserta persona (+ teléfono) y pasajero. Devuelve el pasajero con su id asignado. */
    Pasajero insertar(Pasajero pasajero);
    List<Pasajero> listarTodos();
}
