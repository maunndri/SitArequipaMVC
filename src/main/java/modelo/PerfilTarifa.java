package modelo;

import java.math.BigDecimal;

public enum PerfilTarifa {
    GENERAL(1, new BigDecimal("1.30")),
    UNIVERSITARIO(2, new BigDecimal("0.70")),
    ESCOLAR(3, new BigDecimal("0.50"));

    private final int id;
    private final BigDecimal tarifa;

    PerfilTarifa(int id, BigDecimal tarifa) {
        this.id = id;
        this.tarifa = tarifa;
    }

    public int getId() { return id; }

    public BigDecimal getTarifa() { return tarifa; }

    public static PerfilTarifa desdeId(int id) {
        for (PerfilTarifa p : values()) if (p.id == id) return p;
        throw new IllegalArgumentException("Perfil de tarifa desconocido: " + id);
    }
}
