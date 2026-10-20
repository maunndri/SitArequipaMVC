package modelo;

public enum EstadoTarjeta {
    ACTIVA(1), BLOQUEADA(2), ANULADA(3);

    private final int id;
    EstadoTarjeta(int id) { this.id = id; }
    public int getId() { return id; }

    public static EstadoTarjeta desdeId(int id) {
        for (EstadoTarjeta e : values()) if (e.id == id) return e;
        throw new IllegalArgumentException("Estado de tarjeta desconocido: " + id);
    }
}
