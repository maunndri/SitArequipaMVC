package modelo;

import java.math.BigDecimal;

public class Recarga {
    private final String numeroTarjeta;
    private final int idOperador;
    private final String numeroComprobante;
    private final BigDecimal monto;
    private final BigDecimal saldoAnterior;
    private final BigDecimal saldoPosterior;

    public Recarga(String numeroTarjeta, int idOperador, String numeroComprobante,
                   BigDecimal monto, BigDecimal saldoAnterior, BigDecimal saldoPosterior) {
        this.numeroTarjeta = numeroTarjeta; this.idOperador = idOperador;
        this.numeroComprobante = numeroComprobante; this.monto = monto;
        this.saldoAnterior = saldoAnterior; this.saldoPosterior = saldoPosterior;
    }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public int getIdOperador() { return idOperador; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
}
