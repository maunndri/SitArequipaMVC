package modelo;

public enum MotivoRechazo {
    SALDO_INSUFICIENTE(1, "Saldo insuficiente"),
    TARJETA_BLOQUEADA(2, "Tarjeta bloqueada"),
    TARJETA_INEXISTENTE(3, "Tarjeta no registrada"),
    TARJETA_ANULADA(4, "Tarjeta anulada");

    private final int id;
    private final String descripcion;
    MotivoRechazo(int id, String descripcion) { this.id = id; this.descripcion = descripcion; }
    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }
}
