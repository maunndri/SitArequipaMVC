package dao;

import java.util.Optional;
import modelo.Ruta;

public interface RutaDAO {
    Optional<Ruta> buscarPorId(int idRuta);
}
