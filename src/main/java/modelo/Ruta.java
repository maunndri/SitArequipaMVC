package modelo;

import java.math.BigDecimal;

public class Ruta {
    private final int id;
    private final String codigo;
    private final String nombre;
    private final BigDecimal tarifaBase;
    private final boolean activa;

    public Ruta(int id, String codigo, String nombre, BigDecimal tarifaBase, boolean activa) {
        this.id = id; this.codigo = codigo; this.nombre = nombre;
        this.tarifaBase = tarifaBase; this.activa = activa;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getTarifaBase() { return tarifaBase; }
    public boolean isActiva() { return activa; }
}
