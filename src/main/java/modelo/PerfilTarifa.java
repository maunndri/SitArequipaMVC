package modelo;

public enum PerfilTarifa {
    GENERAL(1), UNIVERSITARIO(2), ESCOLAR(3);

    private final int id;
    PerfilTarifa(int id) { this.id = id; }
    public int getId() { return id; }

    public static PerfilTarifa desdeId(int id) {
        for (PerfilTarifa p : values()) if (p.id == id) return p;
        throw new IllegalArgumentException("Perfil de tarifa desconocido: " + id);
    }
}
