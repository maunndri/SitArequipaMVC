package modelo;

import java.math.BigDecimal;

public class Viaje {
    private final String numeroTarjeta;
    private final String idLector;
    private final int idRuta;
    private final EstadoViaje estado;
    private final BigDecimal tarifa;

    public Viaje(String numeroTarjeta, String idLector, int idRuta, EstadoViaje estado, BigDecimal tarifa) {
        this.numeroTarjeta = numeroTarjeta; this.idLector = idLector; this.idRuta = idRuta;
        this.estado = estado; this.tarifa = tarifa;
    }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public String getIdLector() { return idLector; }
    public int getIdRuta() { return idRuta; }
    public EstadoViaje getEstado() { return estado; }
    public BigDecimal getTarifa() { return tarifa; }
}
