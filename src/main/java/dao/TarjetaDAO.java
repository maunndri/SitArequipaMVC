package dao;

import java.math.BigDecimal;
import java.util.Optional;
import modelo.Tarjeta;

public interface TarjetaDAO {
    Optional<Tarjeta> buscarPorNumero(String numero);
    /** SELECT ... FOR UPDATE: bloquea la fila hasta el fin de la transacción (evita doble cobro). */
    Optional<Tarjeta> buscarParaActualizar(String numero);
    boolean existeActivaDePasajero(int idPasajero);
    void insertar(Tarjeta tarjeta);
    void actualizarSaldo(String numero, BigDecimal nuevoSaldo);
}
