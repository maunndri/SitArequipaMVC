package modelo;

public enum EstadoViaje {
    APROBADO(1), RECHAZADO(2);

    private final int id;
    EstadoViaje(int id) { this.id = id; }
    public int getId() { return id; }
}
