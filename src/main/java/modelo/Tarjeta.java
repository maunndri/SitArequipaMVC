package modelo;

import excepcion.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Entidad central. Las reglas de saldo viven aquí (modelo rico), no en el servlet. */
public class Tarjeta {
    private final String numero;
    private final int idPasajero;
    private final PerfilTarifa perfil;
    private final EstadoTarjeta estado;
    private BigDecimal saldo;
    private final LocalDate fechaEmision;

    public Tarjeta(String numero, int idPasajero, PerfilTarifa perfil, EstadoTarjeta estado,
                   BigDecimal saldo, LocalDate fechaEmision) {
        this.numero = numero; this.idPasajero = idPasajero; this.perfil = perfil;
        this.estado = estado; this.saldo = saldo; this.fechaEmision = fechaEmision;
    }

    public boolean estaActiva() { return estado == EstadoTarjeta.ACTIVA; }

    public boolean tieneSaldoPara(BigDecimal tarifa) { return saldo.compareTo(tarifa) >= 0; }

    public void acreditar(BigDecimal monto) {
        if (monto.signum() <= 0) throw new NegocioException("El monto debe ser mayor a cero.");
        saldo = saldo.add(monto);
    }

    /** Regla RF-004: nunca se permite saldo negativo. */
    public void debitar(BigDecimal monto) {
        if (!tieneSaldoPara(monto)) throw new NegocioException("Saldo insuficiente.");
        saldo = saldo.subtract(monto);
    }

    public String getNumero() { return numero; }
    public int getIdPasajero() { return idPasajero; }
    public PerfilTarifa getPerfil() { return perfil; }
    public EstadoTarjeta getEstado() { return estado; }
    public BigDecimal getSaldo() { return saldo; }
    public LocalDate getFechaEmision() { return fechaEmision; }
}
