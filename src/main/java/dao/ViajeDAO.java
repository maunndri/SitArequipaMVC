package dao;

import modelo.MotivoRechazo;
import modelo.Viaje;

public interface ViajeDAO {
    int insertar(Viaje viaje);
    void insertarRechazo(int idViaje, MotivoRechazo motivo);
}
